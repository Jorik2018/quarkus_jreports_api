/*     */ package WEB-INF.classes.gob.regionancash.tesoreria.rest;
/*     */ 
/*     */ import gob.regionancash.tesoreria.ejb.WarrantTypeFacadeLocal;
/*     */ import gob.regionancash.tesoreria.jpa.WarrantType;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.ejb.EJB;
/*     */ import javax.ejb.Stateless;
/*     */ import javax.sql.DataSource;
/*     */ import javax.ws.rs.Consumes;
/*     */ import javax.ws.rs.DELETE;
/*     */ import javax.ws.rs.GET;
/*     */ import javax.ws.rs.POST;
/*     */ import javax.ws.rs.PUT;
/*     */ import javax.ws.rs.Path;
/*     */ import javax.ws.rs.PathParam;
/*     */ import javax.ws.rs.Produces;
/*     */ import javax.ws.rs.QueryParam;
/*     */ import javax.ws.rs.core.Response;
/*     */ import org.isobit.app.ws.AbstractFacadeREST;
/*     */ import org.isobit.jreport.JR;
/*     */ import org.isobit.util.XDate;
/*     */ import org.isobit.util.XUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ @Stateless
/*     */ @Path("warrant-type")
/*     */ @Produces({"application/json;charset=UTF-8"})
/*     */ public class WarrantTypeFacadeREST
/*     */   extends AbstractFacadeREST<WarrantType>
/*     */ {
/*     */   @EJB
/*     */   private WarrantTypeFacadeLocal warrantFacade;
/*     */   
/*     */   @POST
/*     */   @Consumes({"application/json;charset=UTF-8"})
/*     */   public void create(WarrantType entity) {
/*  41 */     this.warrantFacade.edit(entity);
/*     */   }
/*     */   
/*     */   @PUT
/*     */   @Path("{id}")
/*     */   @Consumes({"application/json;charset=UTF-8"})
/*     */   public void edit(@PathParam("id") Integer id, WarrantType entity) {
/*  48 */     this.warrantFacade.edit(entity);
/*     */   }
/*     */ 
/*     */   
/*     */   @DELETE
/*     */   @Path("{id}")
/*     */   public void remove(@PathParam("id") Integer id) {}
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("{id}")
/*     */   public Object get(@PathParam("id") Integer id) {
/*  60 */     return this.warrantFacade.find(id);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @GET
/*     */   public Object findRange(@QueryParam("people") String employee, @QueryParam("peopleId") Integer peopleId, @QueryParam("position") String position, @QueryParam("code") String code, @QueryParam("active") Integer active, @QueryParam("dependency") String dependency, @QueryParam("dependencyId") Integer dependencyId, @QueryParam("query") String query) {
/*  74 */     return findRange(Integer.valueOf(0), Integer.valueOf(0), employee, peopleId, position, code, active, dependency, dependencyId, query);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("{from}/{to}")
/*     */   public Object findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @QueryParam("people") String employee, @QueryParam("peopleId") Integer peopleId, @QueryParam("position") String position, @QueryParam("code") String code, @QueryParam("active") Integer active, @QueryParam("dependency") String dependency, @QueryParam("dependencyId") Integer dependencyId, @QueryParam("query") String query) {
/*  90 */     Map<Object, Object> m = new HashMap<>();
/*  91 */     if (position != null) {
/*  92 */       m.put("position", position);
/*     */     }
/*  94 */     if (query != null) {
/*  95 */       m.put("query", query);
/*     */     }
/*  97 */     if (code != null) {
/*  98 */       m.put("code", code);
/*     */     }
/* 100 */     if (active != null) {
/* 101 */       m.put("active", active);
/*     */     }
/* 103 */     if (peopleId != null) {
/* 104 */       m.put("people.id", peopleId);
/*     */     }
/* 106 */     if (employee != null) {
/* 107 */       m.put("people", employee);
/*     */     }
/*     */     
/* 110 */     if (dependency != null) {
/* 111 */       m.put("dependency", dependency);
/*     */     }
/* 113 */     if (XUtil.intValue(dependencyId) != 0) {
/* 114 */       m.put("dependency", dependencyId);
/*     */     }
/* 116 */     List ll = this.warrantFacade.load(from.intValue(), to.intValue(), null, m);
/*     */     
/* 118 */     m.put("data", ll);
/* 119 */     return m;
/*     */   }
/*     */   
/*     */   @GET
/*     */   @Path("count")
/*     */   @Produces({"text/plain"})
/*     */   public String countREST() {
/* 126 */     return String.valueOf(this.warrantFacade.count());
/*     */   }
/*     */   
/*     */   @POST
/*     */   @Path("download")
/*     */   @Produces({"application/octet-stream"})
/*     */   @Consumes({"application/json"})
/*     */   public Response downloadReport(Map<?, ?> m) {
/* 134 */     Map<Object, Object> p = new HashMap<>();
/* 135 */     p.putAll(m);
/* 136 */     if (p.containsKey("FORMAT")) {
/* 137 */       p.put(JR.EXTENSION, p.get("FORMAT"));
/*     */     }
/* 139 */     if (p.get("FECHA_FIN") instanceof String) {
/* 140 */       p.put("FECHA_FIN", XDate.parse(p.get("FECHA_FIN").toString(), "dd/MM/yyyy"));
/*     */     }
/* 142 */     if (p.get("FECHA_INI") instanceof String) {
/* 143 */       p.put("FECHA_INI", XDate.parse(p.get("FECHA_INI").toString(), "dd/MM/yyyy"));
/*     */     }
/* 145 */     XDate.setDefaultFormat("dd/MM/yyyy");
/* 146 */     List<WarrantType> data = this.warrantFacade.load(0, 0, null, p);
/*     */     
/* 148 */     if (p.containsKey("FECHA_FIN")) {
/* 149 */       p.put("FECHA_FIN", XDate.format((Date)p.get("FECHA_FIN")));
/*     */     }
/* 151 */     if (p.containsKey("FECHA_INI")) {
/* 152 */       p.put("FECHA_INI", XDate.format((Date)p.get("FECHA_INI")));
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 158 */     p.put("IS_ONE_PAGE_PER_SHEET", Boolean.valueOf(false));
/* 159 */     p.put("SIGN_SECTION", Boolean.valueOf(true));
/* 160 */     p.put(DataSource.class, data);
/* 161 */     int opt = XUtil.intValue(m.get("option"));
/*     */     
/* 163 */     switch (opt) {
/*     */       case 1:
/* 165 */         jr = "cartaFianza_1";
/*     */         break;
/*     */       default:
/* 168 */         jr = "cartaFianza"; break;
/*     */     } 
/* 170 */     switch (XUtil.intValue(m.get("group"))) {
/*     */       case 1:
/* 172 */         jr = "cartaFianza_x_expediente";
/*     */         break;
/*     */       case 2:
/* 175 */         jr = "cartaFianza_x_proveedor"; break;
/*     */     } 
/* 177 */     String jr = "/gob/regionancash/tesoreria/jr/" + jr + ".jasper";
/* 178 */     p.put("rest", Boolean.valueOf(true));
/* 179 */     Object o = JR.open(jr, p);
/* 180 */     String filename = "";
/* 181 */     return 
/* 182 */       Response.ok(o, "application/octet-stream")
/* 183 */       .header("content-disposition", "attachment; filename = " + filename)
/* 184 */       .build();
/*     */   }
/*     */ }


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/rest/WarrantTypeFacadeREST.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */