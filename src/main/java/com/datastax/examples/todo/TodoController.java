package com.datastax.examples.todo;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.oss.driver.api.core.DriverException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@CrossOrigin(
  methods = {PUT, POST, GET, OPTIONS, DELETE, PATCH},
  maxAge = 3600,
  //allowedHeaders = {"x-requested-with", "origin", "content-type", "accept"},
  allowedHeaders = "*",
  origins = "*"
)
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {
    
    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);
    
    @Autowired
    private TodoRepository todoRepository;
    
    /**
     * Default constructor.
     */
    public TodoController() {}
    
    /**
     * Constructor.
     */
    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }
    
    // --- Operation on the list --
    
    /**
     * Retrieve all tasks (GET)
     */
    @RequestMapping(value = "/", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TodoWebBean>> findAll(HttpServletRequest request) {
        logger.info("List all task in the db: {}", rewriteUrl(request));
        return ResponseEntity.ok(todoRepository.findAll()
                .stream().map(dto -> new TodoWebBean(rewriteUrl(request) + dto.getUuid(), dto))
                .collect(Collectors.toList()));
    }
    
    /**
     * Delete all tasks (DELETE)
     */
    @RequestMapping(value = "/", method = DELETE)
    public ResponseEntity<Void> deleteAll(HttpServletRequest request) {
        logger.info("Delete all task in the db: {}", rewriteUrl(request) + "?" + request.getQueryString());
        todoRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    // --- Unitary operation --
    
    /**
     * CREATE = Create a new Task (POST)
     */
    @RequestMapping(
            value = "/",
            method = POST,
            produces = APPLICATION_JSON_VALUE, 
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<TodoWebBean> create(HttpServletRequest request, 
            @RequestBody 
            TodoWebBeanRequest taskCreationRequest)
    throws URISyntaxException {
        Assert.notNull(taskCreationRequest, "You must provide a Task in BODY");
        Assert.hasLength(taskCreationRequest.getTitle(), "Title is a required field to create a task");
        logger.info("Create new Task at {} with title {}",  
                request.getRequestURL().toString(), taskCreationRequest.getTitle());
        TodoEntity dto = new TodoEntity(UUID.randomUUID(), 
                taskCreationRequest.getTitle(), 
                taskCreationRequest.isCompleted(), 
                taskCreationRequest.getOrder());
        todoRepository.save(dto);
        // Created
        TodoWebBean bean = new TodoWebBean(rewriteUrl(request) + dto.getUuid(), dto);
        return ResponseEntity.created(new URI(bean.getUrl())).body(bean);
    }
    
    /**
     * READ = Find a Task by its id (GET)
     */
    @RequestMapping(
            value = "/{taskId}",
            method = GET,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TodoWebBean> read(HttpServletRequest request,
            @PathVariable(value = "taskId") String taskId) {
        logger.info("Find a task with its id {}", rewriteUrl(request) + "?" + request.getQueryString());
        Assert.hasLength(taskId, "TaskId id is required and should not be null");
        Optional<TodoEntity> myTask = todoRepository.findTodoById(UUID.fromString(taskId));
        // Routing Result
        if (!myTask.isPresent()) {
            logger.warn("Task with uid {} has not been found", taskId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new TodoWebBean(rewriteUrl(request), myTask.get()));
    }
    
    /**
     * Update an existing Task (PATCH)
     */
    @RequestMapping(
            value = "/{taskId}",
            method = PATCH,
            produces = APPLICATION_JSON_VALUE, 
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<TodoWebBean> update(HttpServletRequest request, 
            @PathVariable(value = "taskId") String taskId,
            @RequestBody TodoWebBean taskBody)
    throws URISyntaxException {
        Assert.notNull(taskBody, "You must provide a Task in BODY");
        logger.info("Updating task {}", taskId);
        Optional<TodoEntity> myTask = todoRepository.findTodoById(UUID.fromString(taskId));
        // Routing Result
        if (!myTask.isPresent()) {
            logger.warn("Task with uid {} has not been found", taskId);
            return ResponseEntity.notFound().build();
        }
        TodoEntity existing = myTask.get();
        String newTitle =  taskBody.getTitle();
        if (null != newTitle && !"".equals(newTitle)) {
            existing.setTitle(newTitle);
        }
        existing.setCompleted(taskBody.isCompleted());
        existing.setOrder(taskBody.getOrder());
        todoRepository.save(existing);
        return ResponseEntity.ok(new TodoWebBean(rewriteUrl(request), existing));
    }
    
    /**
     * Delete a Task by its id
     */
    @RequestMapping(value = "/{taskId}", method = DELETE)
    @Operation(summary = "Delete a task from its id of exists", description = "Delete a task from its id of exists")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "No results") } )  
    public ResponseEntity<Void> delete(HttpServletRequest request,
            @Parameter(name="taskId", required=true,
                       description="Unique identifier for the task",
                       example = "6f6c5b47-4e23-4437-ada8-d0a6f79330a2")
            @PathVariable(value = "taskId") String taskId) {
        logger.info("Delete a task with its id {}", request.getRequestURL().toString());
        Assert.hasLength(taskId, "TaskId id is required and should not be null");
        Optional<TodoEntity> myTask = todoRepository.findTodoById(UUID.fromString(taskId));
        // Routing Result
        if (!myTask.isPresent()) {
            logger.warn("Task with uid {} has not been found", taskId);
            return ResponseEntity.notFound().build();
        }
        todoRepository.delete(new TodoEntity(UUID.fromString(taskId)));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Converts {@link IllegalArgumentException} into HTTP 400 bad parameter
     * the response body.
     *
     * @param e The {@link DriverException}.
     * @return The error message to be used as response body.
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String _errorBadRequestHandler(IllegalArgumentException ex) {
        return "Invalid Parameter: " + ex.getMessage();
    }
    
    /**
     * Converts {@link DriverException} into HTTP 500 error codes and outputs the error message as
     * the response body.
     *
     * @param e The {@link DriverException}.
     * @return The error message to be used as response body.
     */
    @ExceptionHandler(DriverException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String _errorDriverHandler(DriverException e) {
      return e.getMessage();
    }
    
    /**
     * As isSecure is still false and https is enforce by gitpod let's 
     * change it.
     */
    private String rewriteUrl(HttpServletRequest request) {
        String myUrl = request.getRequestURL().toString();
        if (myUrl.contains("gitpod")) {
            myUrl = myUrl.replaceAll("http", "https");
        }
        return myUrl;
    }

}
