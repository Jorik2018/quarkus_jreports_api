/*     */ package WEB-INF.classes.gob.regionancash.tesoreria.rest;
/*     */ 
/*     */ import gob.regionancash.tesoreria.ejb.WarrantFacadeLocal;
/*     */ import gob.regionancash.tesoreria.jpa.Check;
/*     */ import gob.regionancash.tesoreria.jpa.Warrant;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.lang.reflect.Field;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.ejb.EJB;
/*     */ import javax.ejb.Stateless;
/*     */ import javax.sql.DataSource;
/*     */ import javax.validation.constraints.Size;
/*     */ import javax.ws.rs.Consumes;
/*     */ import javax.ws.rs.DELETE;
/*     */ import javax.ws.rs.GET;
/*     */ import javax.ws.rs.POST;
/*     */ import javax.ws.rs.PUT;
/*     */ import javax.ws.rs.Path;
/*     */ import javax.ws.rs.PathParam;
/*     */ import javax.ws.rs.Produces;
/*     */ import javax.ws.rs.QueryParam;
/*     */ import javax.ws.rs.client.Client;
/*     */ import javax.ws.rs.client.ClientBuilder;
/*     */ import javax.ws.rs.client.Entity;
/*     */ import javax.ws.rs.core.Response;
/*     */ import org.isobit.app.X;
/*     */ import org.isobit.app.ws.AbstractFacadeREST;
/*     */ import org.isobit.jreport.JR;
/*     */ import org.isobit.util.ColumnProperty;
/*     */ import org.isobit.util.XDate;
/*     */ import org.isobit.util.XUtil;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Stateless
/*     */ @Path("check")
/*     */ @Produces({"application/json;charset=UTF-8"})
/*     */ public class CheckFacadeREST
/*     */   extends AbstractFacadeREST<Warrant>
/*     */ {
/*     */   @EJB
/*     */   private WarrantFacadeLocal warrantFacade;
/*  52 */   private Client client = ClientBuilder.newClient();
/*     */ 
/*     */ 
/*     */   
/*     */   @POST
/*     */   @Path("import/{fileName}")
/*     */   @Produces({"application/json;charset=UTF-8"})
/*     */   public Object importFile(@PathParam("fileName") String fileName) throws IOException, ClassNotFoundException {
/*  60 */     InputStream is2 = (InputStream)this.client.target("http://localhost:" + X.getRequest().getLocalPort() + "/xls/api/jao/" + fileName).request().post(Entity.text(""), InputStream.class);
/*  61 */     ArrayList<Check> data = new ArrayList();
/*     */     try {
/*  63 */       ObjectInputStream ois = new ObjectInputStream(is2);
/*  64 */       List<Object[]> l = (List<Object[]>)((Object[])ois.readObject())[0];
/*  65 */       Object[] header = l.get(0);
/*  66 */       Map<Object, ColumnProperty> columns = new HashMap<>();
/*  67 */       Field[] fields = (new Check()).getClass().getDeclaredFields();
/*  68 */       for (Field field : fields) {
/*  69 */         for (int j = 0; j < header.length; j++) {
/*  70 */           if (field.getName().equalsIgnoreCase(("" + header[j]).replace(" ", "").replace("_", ""))) {
/*  71 */             ColumnProperty co = new ColumnProperty();
/*  72 */             columns.put(field.getName(), co);
/*  73 */             Size size = field.<Size>getAnnotation(Size.class);
/*  74 */             if (size != null) {
/*  75 */               co.setSize(size.max());
/*     */             }
/*  77 */             field.setAccessible(true);
/*  78 */             co.setField(field);
/*  79 */             co.setIndex(j);
/*  80 */             co.setName(field.getName());
/*     */             break;
/*     */           } 
/*     */         } 
/*     */       } 
/*  85 */       for (int r = 1; r < l.size(); r++) {
/*  86 */         Object[] row = l.get(r);
/*  87 */         Check check = new Check();
/*  88 */         check.setId(r);
/*  89 */         for (ColumnProperty entry : columns.values()) {
/*  90 */           Object v = (entry.getIndex() < row.length) ? row[entry.getIndex()] : null;
/*  91 */           if (v != null) {
/*     */             
/*  93 */             if (entry.getField().getType().isAssignableFrom(String.class)) {
/*     */               
/*     */               try {
/*  96 */                 entry.getField().set(check, entry.adjust(v));
/*  97 */               } catch (Exception ex) {
/*  98 */                 throw new RuntimeException(ex);
/*     */               }  continue;
/* 100 */             }  if (entry.getField().getType().isAssignableFrom(Integer.class)) {
/*     */               try {
/* 102 */                 entry.getField().set(check, Integer.valueOf(XUtil.intValue(v)));
/* 103 */               } catch (Exception ex) {
/* 104 */                 throw new RuntimeException(ex);
/*     */               }  continue;
/* 106 */             }  if (entry.getField().getType().isAssignableFrom(Double.class)) {
/*     */               try {
/* 108 */                 entry.getField().set(check, Double.valueOf(XUtil.doubleValue(v)));
/* 109 */               } catch (Exception ex) {
/* 110 */                 throw new RuntimeException(ex);
/*     */               }  continue;
/* 112 */             }  if (entry.getField().getType().isAssignableFrom(Date.class)) {
/*     */               try {
/* 114 */                 entry.getField().set(check, v);
/* 115 */               } catch (Exception ex) {
/* 116 */                 throw new RuntimeException(ex);
/*     */               } 
/*     */             }
/*     */           } 
/*     */         } 
/* 121 */         data.add(check);
/*     */       } 
/* 123 */     } catch (IOException ex) {
/* 124 */       throw new RuntimeException(ex);
/*     */     } 
/* 126 */     return data;
/*     */   }
/*     */ 
/*     */   
/*     */   @POST
/*     */   @Consumes({"application/json;charset=UTF-8"})
/*     */   public void create(Warrant entity) {
/* 133 */     this.warrantFacade.edit(entity);
/*     */   }
/*     */   
/*     */   @PUT
/*     */   @Path("{id}")
/*     */   @Consumes({"application/json;charset=UTF-8"})
/*     */   public void edit(@PathParam("id") Integer id, Warrant entity) {
/* 140 */     this.warrantFacade.edit(entity);
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
/* 152 */     return this.warrantFacade.find(id);
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
/* 166 */     return findRange(Integer.valueOf(0), Integer.valueOf(0), employee, peopleId, position, code, active, dependency, dependencyId, query);
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
/* 182 */     Map<Object, Object> m = new HashMap<>();
/* 183 */     if (position != null) {
/* 184 */       m.put("position", position);
/*     */     }
/* 186 */     if (query != null) {
/* 187 */       m.put("query", query);
/*     */     }
/* 189 */     if (code != null) {
/* 190 */       m.put("code", code);
/*     */     }
/* 192 */     if (active != null) {
/* 193 */       m.put("active", active);
/*     */     }
/* 195 */     if (peopleId != null) {
/* 196 */       m.put("people.id", peopleId);
/*     */     }
/* 198 */     if (employee != null) {
/* 199 */       m.put("people", employee);
/*     */     }
/*     */     
/* 202 */     if (dependency != null) {
/* 203 */       m.put("dependency", dependency);
/*     */     }
/* 205 */     if (XUtil.intValue(dependencyId) != 0) {
/* 206 */       m.put("dependency", dependencyId);
/*     */     }
/* 208 */     List ll = this.warrantFacade.load(from.intValue(), to.intValue(), null, m);
/*     */     
/* 210 */     m.put("data", ll);
/* 211 */     return m;
/*     */   }
/*     */   
/*     */   @GET
/*     */   @Path("count")
/*     */   @Produces({"text/plain"})
/*     */   public String countREST() {
/* 218 */     return String.valueOf(this.warrantFacade.count());
/*     */   }
/*     */   
/*     */   @POST
/*     */   @Path("download")
/*     */   @Produces({"application/octet-stream"})
/*     */   @Consumes({"application/json"})
/*     */   public Response downloadReport(Map<?, ?> m) {
/* 226 */     Map<Object, Object> p = new HashMap<>();
/* 227 */     p.putAll(m);
/* 228 */     if (p.containsKey("FORMAT")) {
/* 229 */       p.put(JR.EXTENSION, p.get("FORMAT"));
/*     */     }
/* 231 */     if (p.get("FECHA_FIN") instanceof String) {
/* 232 */       p.put("FECHA_FIN", XDate.parse(p.get("FECHA_FIN").toString(), "dd/MM/yyyy"));
/*     */     }
/* 234 */     if (p.get("FECHA_INI") instanceof String) {
/* 235 */       p.put("FECHA_INI", XDate.parse(p.get("FECHA_INI").toString(), "dd/MM/yyyy"));
/*     */     }
/* 237 */     XDate.setDefaultFormat("dd/MM/yyyy");
/* 238 */     List<Warrant> data = this.warrantFacade.load(0, 0, null, p);
/*     */     
/* 240 */     if (p.containsKey("FECHA_FIN")) {
/* 241 */       p.put("FECHA_FIN", XDate.format((Date)p.get("FECHA_FIN")));
/*     */     }
/* 243 */     if (p.containsKey("FECHA_INI")) {
/* 244 */       p.put("FECHA_INI", XDate.format((Date)p.get("FECHA_INI")));
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 250 */     p.put("IS_ONE_PAGE_PER_SHEET", Boolean.valueOf(false));
/* 251 */     p.put("SIGN_SECTION", Boolean.valueOf(true));
/* 252 */     p.put(DataSource.class, data);
/* 253 */     int opt = XUtil.intValue(m.get("option"));
/*     */     
/* 255 */     switch (opt) {
/*     */       case 1:
/* 257 */         jr = "cartaFianza_1";
/*     */         break;
/*     */       default:
/* 260 */         jr = "cartaFianza"; break;
/*     */     } 
/* 262 */     switch (XUtil.intValue(m.get("group"))) {
/*     */       case 1:
/* 264 */         jr = "cartaFianza_x_expediente";
/*     */         break;
/*     */       case 2:
/* 267 */         jr = "cartaFianza_x_proveedor"; break;
/*     */     } 
/* 269 */     String jr = "/gob/regionancash/tesoreria/jr/" + jr + ".jasper";
/* 270 */     p.put("rest", Boolean.valueOf(true));
/* 271 */     Object o = JR.open(jr, p);
/* 272 */     String filename = "";
/* 273 */     return 
/* 274 */       Response.ok(o, "application/octet-stream")
/* 275 */       .header("content-disposition", "attachment; filename = " + filename)
/* 276 */       .build();
/*     */   }
/*     */ }


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/rest/CheckFacadeREST.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */