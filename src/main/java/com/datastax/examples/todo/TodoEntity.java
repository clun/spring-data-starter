package com.datastax.examples.todo;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Entity for table and Spring Data.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Table("todo_tasks")
public class TodoEntity implements Serializable, Comparable<TodoEntity> {

    private static final long serialVersionUID = -5844442448334944278L;
    
    @PrimaryKeyColumn(
            name = "uid", ordinal = 0, 
            type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = Name.UUID)
    private UUID uuid;
    
    @Column("title")
    @CassandraType(type = Name.TEXT)
    private String title;

    @Column("completed")
    @CassandraType(type = Name.BOOLEAN)
    private boolean completed = false;

    @Column("offset")
    @CassandraType(type = Name.INT)
    private int order = 0;
    
    public TodoEntity() {}
  
    public TodoEntity(TodoWebBean t) {
        this.uuid       = t.getUuid();
        this.title      = t.getTitle();
        this.order      = t.getOrder();
        this.completed  = t.isCompleted();
    }
    
    public TodoEntity(UUID uuid) {
        this.uuid = uuid;
    }
    
    public TodoEntity(UUID uuid, String title, boolean completed, int order) {
        this.uuid       = uuid;
        this.title      = title;
        this.order      = order;
        this.completed  = completed;
    }
    
    public TodoWebBean mapAsWebBean() {
        TodoWebBean t = new TodoWebBean();
        t.setUuid(this.uuid);
        t.setCompleted(this.completed);
        t.setOrder(this.order);
        t.setTitle(this.title);
        return t;
    }
   
    /** {@inheritDoc} */
    @Override
    public int compareTo(TodoEntity other) {
        if (other == null)
            return 1;
        return order - other.getOrder();
    }

    /**
     * Getter accessor for attribute 'uuid'.
     *
     * @return current value of 'uuid'
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Setter accessor for attribute 'uuid'.
     * 
     * @param uuid
     *            new value for 'uuid '
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Getter accessor for attribute 'title'.
     *
     * @return current value of 'title'
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter accessor for attribute 'title'.
     * 
     * @param title
     *            new value for 'title '
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter accessor for attribute 'completed'.
     *
     * @return current value of 'completed'
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Setter accessor for attribute 'completed'.
     * 
     * @param completed
     *            new value for 'completed '
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Getter accessor for attribute 'order'.
     *
     * @return current value of 'order'
     */
    public int getOrder() {
        return order;
    }

    /**
     * Setter accessor for attribute 'order'.
     * 
     * @param order
     *            new value for 'order '
     */
    public void setOrder(int order) {
        this.order = order;
    }
    
}
