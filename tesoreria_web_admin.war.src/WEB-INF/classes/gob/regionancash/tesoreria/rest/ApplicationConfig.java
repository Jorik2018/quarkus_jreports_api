/*    */ package WEB-INF.classes.gob.regionancash.tesoreria.rest;
/*    */ import gob.regionancash.tesoreria.rest.CheckFacadeREST;
/*    */ import gob.regionancash.tesoreria.rest.JacksonConfig;
/*    */ import java.util.Set;
/*    */ import javax.ws.rs.core.Application;
/*    */ import org.isobit.app.rest.CORSFilter;
/*    */ 
/*    */ @ApplicationPath("api")
/*    */ public class ApplicationConfig extends Application {
/*    */   public Set<Class<?>> getClasses() {
/* 11 */     Set<Class<?>> resources = new HashSet<>();
/* 12 */     addRestResourceClasses(resources);
/* 13 */     resources.add(JacksonConfig.class);
/* 14 */     return resources;
/*    */   }
/*    */   
/*    */   private void addRestResourceClasses(Set<Class<?>> resources) {
/* 18 */     resources.add(CheckFacadeREST.class);
/* 19 */     resources.add(JacksonConfig.class);
/* 20 */     resources.add(WarrantFacadeREST.class);
/* 21 */     resources.add(WarrantTypeFacadeREST.class);
/* 22 */     resources.add(CORSFilter.class);
/* 23 */     resources.add(ExceptionHandler.class);
/* 24 */     resources.add(InstallServlet.class);
/*    */   }
/*    */ }


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/rest/ApplicationConfig.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */