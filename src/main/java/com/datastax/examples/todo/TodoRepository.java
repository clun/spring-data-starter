package com.datastax.examples.todo;

import java.util.Optional;
import java.util.UUID;


import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends CassandraRepository<TodoEntity, UUID> {
    
    @Query("SELECT * FROM todo_tasks WHERE uid = ?0")
    Optional<TodoEntity> findByTaskById(UUID taskid);
    
}

