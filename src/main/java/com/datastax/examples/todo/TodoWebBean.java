package com.datastax.examples.todo;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Object to be exposed in the REST API.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Getter
@Setter
public class TodoWebBean extends TodoWebBeanRequest implements Serializable, Comparable<TodoWebBean>  {

    /** Serial. */
    private static final long serialVersionUID = -7930662827102682933L;
    
    /** Unique id for the task. */
    private UUID uuid;
    
    /** required by todobackend.com specifications. */
    private String url;
    
    /**
     * Constructors.
     */
    public TodoWebBean() {}
    
    /**
     * Constructor helping for tests
     */
    public TodoWebBean(String title) {
        this(UUID.randomUUID(), title, false, -1);
    }
    
    public TodoWebBean(String url, TodoEntity t) {
        this.url        = url;
        this.uuid       = t.getUuid();
        setTitle(t.getTitle());
        setOrder(t.getOrder());
        setCompleted(t.isCompleted());
    }

    /**
     * Full constructor.
     */
    public TodoWebBean(UUID uuid, String title, boolean completed, int order) {
        super();
        this.uuid = uuid;
        setTitle(title);
        setCompleted(completed);
        setOrder(order);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(TodoWebBean other) {
        if (other == null) return 1;
        return this.getOrder() - other.getOrder();
    }
}
