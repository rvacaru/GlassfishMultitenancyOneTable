package eu.clickreply.fw5.glassfishmultitenancy;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;

/**
 * XXX @TransactionAttribute(TransactionAttributeType.REQUIRED) is needed for generating and executing NamedQueries
 * @author r.vacaru
 */

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CtxKidProcessor {
    
    @EJB
    private TenantCtxEntityManager manager; 
                 
    public void perist(Kid k){
        manager.persist(k);
    }
    
    public Kid merge(Kid k){
        return manager.merge(k);
    }
    
    public Kid find(Long id){
        return manager.find(Kid.class, id);
    }
    
    public void remove(Kid k){
        manager.remove(k);
    }
    
    public void removeAll(){
      Query q = manager.createNamedQuery(Kid.REMOVE_ALL);
      System.out.println(q.executeUpdate());
    }
    
    public List<Kid> selectAll(){
      Query q = manager.createNamedQuery(Kid.SELECT_ALL);
      return (List<Kid>) q.getResultList();
    }
}