/*    */ package WEB-INF.classes.gob.regionancash.tesoreria.ejb;
/*    */ 
/*    */ import gob.regionancash.tesoreria.ejb.WarrantTypeFacadeLocal;
/*    */ import gob.regionancash.tesoreria.jpa.WarrantType;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import javax.ejb.Stateless;
/*    */ import javax.persistence.EntityManager;
/*    */ import javax.persistence.Query;
/*    */ import org.isobit.util.AbstractFacade;
/*    */ import org.isobit.util.XUtil;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @Stateless
/*    */ public class WarrantTypeFacade
/*    */   extends AbstractFacade<WarrantType>
/*    */   implements WarrantTypeFacadeLocal
/*    */ {
/*    */   public List<WarrantType> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
/* 23 */     Object filter = XUtil.isEmpty(filters.get("filter"), null);
/* 24 */     List<Query> ql = new ArrayList<>();
/*    */     
/* 26 */     EntityManager em = getEntityManager(); String sql;
/* 27 */     ql.add(em.createQuery("SELECT o " + (sql = "FROM WarrantType o WHERE 1=1 " + ((filter != null) ? " AND UPPER(o.name) like :filter" : "")) + "  ORDER BY 1 ASC"));
/*    */ 
/*    */     
/* 30 */     if (pageSize > 0) {
/* 31 */       ((Query)ql.get(0)).setFirstResult(first).setMaxResults(pageSize);
/* 32 */       ql.add(em.createQuery("SELECT COUNT(o) " + sql));
/*    */     } 
/* 34 */     for (Query q : ql) {
/* 35 */       if (filter != null) {
/* 36 */         q.setParameter("filter", "%" + filter.toString().toUpperCase().replace(" ", "%") + "%");
/*    */       }
/*    */     } 
/* 39 */     if (pageSize > 0) {
/* 40 */       filters.put("size", ((Query)ql.get(1)).getSingleResult());
/*    */     }
/* 42 */     return ((Query)ql.get(0)).getResultList();
/*    */   }
/*    */ 
/*    */   
/*    */   public void edit(WarrantType entity) {
/* 47 */     if (entity.getId() == null) {
/* 48 */       create(entity);
/*    */     } else {
/* 50 */       super.edit(entity);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/ejb/WarrantTypeFacade.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */