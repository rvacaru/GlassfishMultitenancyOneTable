package eu.clickreply.fw5.glassfishmultitenancy;

import static eu.clickreply.fw5.glassfishmultitenancy.Kid.*;
import java.io.Serializable;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

/** Person Entity to be stored in the databases for the load tests
 * XXX Autogeneration of the ID
 * To have the autogeneration of the Long ID with the @SequenceGenerator you have to:
 * ORACLE12G: create a sequence with first value 1 and cache 5 (NB don't set a trigger to the sequence in the related table)
 * MySQL: execute the sql script ../META-INF/sql/createAndFill_hibernatesequence.sql
 * XXX Composite PK Autogeneration in MySQL (tenant_id, id) needs a trigger: 
 * http://stackoverflow.com/questions/18120088/defining-composite-key-with-auto-increment-in-mysql
 * @Entity @Table(name = "KIDS")
 */
@Entity @Table(name = "KIDS")
@Multitenant
@TransactionManagement(TransactionManagementType.CONTAINER) //default one
@NamedQueries({
    @NamedQuery(name = REMOVE_ALL, query = "delete from Kid"),
    @NamedQuery(name = SELECT_ALL, query = "select k from Kid k")
})
@TenantDiscriminatorColumn(name = "TENANT_ID", contextProperty="eclipselink.tenant_id", primaryKey = false)
public class Kid implements Serializable {
    
    public static final String REMOVE_ALL = "Kid.removeAll";
    public static final String SELECT_ALL = "Kid.selectAll";
       
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @NotNull
    private int age;

    public Kid(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public Long getId() {
            return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }
     
    // required by JPA
    protected Kid() {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + this.age;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Kid other = (Kid) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.age != other.age) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Kid{" + "id=" + id + ", name=" + name + ", age=" + age + '}';
    }   
}