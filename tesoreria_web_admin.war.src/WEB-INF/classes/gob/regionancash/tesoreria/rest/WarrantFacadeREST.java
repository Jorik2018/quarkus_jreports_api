/*     */ package WEB-INF.classes.gob.regionancash.tesoreria.rest;
/*     */ 
/*     */ import gob.regionancash.tesoreria.ejb.WarrantFacadeLocal;
/*     */ import gob.regionancash.tesoreria.jpa.Warrant;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Calendar;
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
/*     */ import javax.ws.rs.client.ClientBuilder;
/*     */ import javax.ws.rs.core.Response;
/*     */ import org.isobit.app.X;
/*     */ import org.isobit.app.ejb.SystemFacadeLocal;
/*     */ import org.isobit.app.ws.AbstractFacadeREST;
/*     */ import org.isobit.jreport.JR;
/*     */ import org.isobit.util.XDate;
/*     */ import org.isobit.util.XFile;
/*     */ import org.isobit.util.XMap;
/*     */ import org.isobit.util.XUtil;
/*     */ 
/*     */ @Stateless
/*     */ @Path("warrant")
/*     */ @Produces({"application/json;charset=UTF-8"})
/*     */ public class WarrantFacadeREST
/*     */   extends AbstractFacadeREST<Warrant> {
/*     */   @EJB
/*     */   private WarrantFacadeLocal warrantFacade;
/*     */   private static String UPLOAD_DIR;
/*     */   @EJB
/*     */   private SystemFacadeLocal systemFacade;
/*     */   
/*     */   private String getUploadDir() {
/*  47 */     if (UPLOAD_DIR == null)
/*     */     {
/*     */       
/*  50 */       UPLOAD_DIR = (String)ClientBuilder.newClient().target("http://localhost:" + X.getRequest().getLocalPort() + "/api/system/upload-dir").request().get(String.class);
/*     */     }
/*  52 */     return UPLOAD_DIR;
/*     */   }
/*     */ 
/*     */   
/*     */   @POST
/*     */   @Consumes({"application/json;charset=UTF-8"})
/*     */   public void create(Warrant entity) {
/*  59 */     Map ext = (Map)entity.getExt();
/*  60 */     if (XUtil.booleanValue(ext.get("delete-upload"))) {
/*  61 */       entity.setUpload(Boolean.valueOf(false));
/*     */     }
/*  63 */     this.warrantFacade.edit(entity);
/*  64 */     String df = (String)ext.get("tempFile");
/*  65 */     if (!XUtil.isEmpty(df)) {
/*     */       
/*     */       try {
/*     */         
/*  69 */         File f = new File(File.createTempFile("temp-file-name", ".tmp").getParentFile(), df);
/*  70 */         String path = getFilePath();
/*     */         
/*  72 */         String fn = XFile.simplifyFileName("CF-" + String.format("%04d", new Object[] { entity.getId() }) + "-" + entity.getExpediente() + "-" + entity.getNroCarta());
/*  73 */         if (XFile.copy(f, new File(XFile.getFile(path).getCanonicalPath(), fn + "." + XFile.getFileExtension(f))) == null) {
/*  74 */           throw new RuntimeException("El documento no pudo ser grabado");
/*     */         }
/*  76 */         entity.setExtension(XFile.getFileExtension(f));
/*  77 */         entity.setUpload(Boolean.valueOf(true));
/*  78 */         this.warrantFacade.edit(entity);
/*     */       }
/*  80 */       catch (IOException iOException) {}
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public String getFileName(Warrant selected) {
/*  86 */     return XFile.simplifyFileName("CF-" + String.format("%04d", new Object[] { selected.getId() }) + "-" + selected.getExpediente() + "-" + selected.getNroCarta()) + "." + selected.getExtension();
/*     */   }
/*     */   
/*     */   public String getFilePath() {
/*  90 */     return getUploadDir() + "/tesoreria/cartaFianza";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @PUT
/*     */   @Path("{id}")
/*     */   @Consumes({"application/json;charset=UTF-8"})
/*     */   public void edit(@PathParam("id") Integer id, Warrant entity) {
/* 102 */     this.warrantFacade.edit(entity);
/*     */   }
/*     */   
/*     */   @DELETE
/*     */   @Path("{id}")
/*     */   public void remove(@PathParam("id") Integer id) {
/* 108 */     Warrant w = this.warrantFacade.find(id);
/* 109 */     w.setCanceled(true);
/* 110 */     this.warrantFacade.edit(w);
/*     */   }
/*     */   
/*     */   @POST
/*     */   @Path("save-file")
/*     */   public void saveFile(Map m) {
/* 116 */     System.out.println(m);
/*     */     try {
/* 118 */       Warrant entity = this.warrantFacade.find(Integer.valueOf(XUtil.intValue(m.get("id"))));
/* 119 */       String df = (String)m.get("tempFile");
/* 120 */       File tempFile = new File(File.createTempFile("temp-file-name", ".tmp").getParentFile(), df);
/* 121 */       String path = getFilePath();
/*     */       
/* 123 */       String fn = XFile.simplifyFileName("CF-" + String.format("%04d", new Object[] { entity.getId() }) + "-" + entity.getExpediente() + "-" + entity.getNroCarta());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 130 */       if (XFile.copy(tempFile, new File(
/*     */             
/* 132 */             XFile.getFile(path).getCanonicalPath(), fn + "." + XFile.getFileExtension(tempFile))) == null) {
/* 133 */         throw new RuntimeException("El documento no pudo ser grabado");
/*     */       }
/* 135 */       entity.setExtension(XFile.getFileExtension(tempFile));
/* 136 */       entity.setUpload(Boolean.valueOf(true));
/* 137 */       this.warrantFacade.edit(entity);
/*     */     }
/* 139 */     catch (IOException iOException) {}
/*     */   }
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("create/{id}")
/*     */   public Object create(@PathParam("id") Integer id) {
/* 146 */     Warrant w = new Warrant();
/* 147 */     if (XUtil.intValue(id) > 0) {
/* 148 */       Warrant o = this.warrantFacade.find(id);
/* 149 */       if (o != null) {
/* 150 */         w.setExpediente(o.getExpediente());
/* 151 */         w.setProveedor(o.getProveedor());
/* 152 */         w.setProcessType(o.getProcessType());
/* 153 */         w.setEntidad(o.getEntidad());
/* 154 */         w.setObra(o.getObra());
/*     */       } 
/*     */     } 
/* 157 */     if (w.getExpediente() == null) {
/* 158 */       w.setExpediente(Integer.valueOf(1 + this.warrantFacade.getMaxExpediente()));
/*     */     }
/* 160 */     w.setExt(new HashMap<>());
/* 161 */     return w;
/*     */   }
/*     */   
/*     */   @GET
/*     */   @Path("{id}")
/*     */   public Object get(@PathParam("id") Integer id) {
/* 167 */     Warrant w = this.warrantFacade.find(id);
/* 168 */     HashMap<Object, Object> hm = new HashMap<>();
/* 169 */     hm.put("src", getFileName(w));
/* 170 */     w.setExt(hm);
/* 171 */     return w;
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
/*     */ 
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("{from}/{to}")
/*     */   public Object findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @QueryParam("people") String employee, @QueryParam("peopleId") Integer peopleId, @QueryParam("provider") String provider, @QueryParam("obra") String obra, @QueryParam("expediente") String expediente, @QueryParam("code") String code, @QueryParam("warrantType") String warrantType, @QueryParam("active") Integer active, @QueryParam("entidad") String entidad, @QueryParam("dependencyId") Integer dependencyId, @QueryParam("faltan") Integer faltan, @QueryParam("query") String query, @QueryParam("order") String order) {
/* 190 */     Map<Object, Object> m = new HashMap<>();
/*     */     
/* 192 */     if (warrantType != null) {
/* 193 */       m.put("warrantType", warrantType.split(","));
/*     */     }
/* 195 */     if (query != null) {
/* 196 */       m.put("query", query);
/*     */     }
/* 198 */     if (expediente != null) {
/* 199 */       m.put("expediente", expediente);
/*     */     }
/* 201 */     if (faltan != null) {
/* 202 */       m.put("faltan", faltan);
/*     */     }
/* 204 */     if (order != null) {
/* 205 */       m.put("order", order);
/*     */     }
/* 207 */     if (code != null) {
/* 208 */       m.put("code", code);
/*     */     }
/* 210 */     if (active != null) {
/* 211 */       m.put("active", active);
/*     */     }
/* 213 */     if (peopleId != null) {
/* 214 */       m.put("people.id", peopleId);
/*     */     }
/* 216 */     if (employee != null) {
/* 217 */       m.put("people", employee);
/*     */     }
/* 219 */     if (obra != null) {
/* 220 */       m.put("obra", obra);
/*     */     }
/* 222 */     if (provider != null) {
/* 223 */       m.put("provider", provider);
/*     */     }
/* 225 */     if (entidad != null) {
/* 226 */       m.put("entidad", entidad);
/*     */     }
/* 228 */     List<Warrant> ll = this.warrantFacade.load(from.intValue(), to.intValue(), null, m);
/* 229 */     for (Warrant w : ll) {
/* 230 */       if (w.getUpload() != null && w.getUpload().booleanValue()) {
/* 231 */         w.setExt(new XMap(new Object[] { "src", getFileName(w) }));
/*     */       }
/*     */     } 
/* 234 */     m.put("data", ll);
/* 235 */     return m;
/*     */   }
/*     */   
/*     */   @GET
/*     */   @Path("count")
/*     */   @Produces({"text/plain"})
/*     */   public String countREST() {
/* 242 */     return String.valueOf(this.warrantFacade.count());
/*     */   }
/*     */   
/*     */   @POST
/*     */   @Path("download")
/*     */   @Produces({"application/octet-stream"})
/*     */   @Consumes({"application/json"})
/*     */   public Response downloadReport(Map<?, ?> m) {
/* 250 */     Map<Object, Object> p = new HashMap<>();
/* 251 */     p.putAll(m);
/* 252 */     if (p.containsKey("FORMAT")) {
/* 253 */       p.put(JR.EXTENSION, p.get("FORMAT"));
/*     */     }
/* 255 */     if (p.get("FECHA_FIN") instanceof String) {
/* 256 */       p.put("FECHA_FIN", XDate.parse(p.get("FECHA_FIN").toString(), "dd/MM/yyyy"));
/*     */     }
/* 258 */     if (p.get("FECHA_INI") instanceof String) {
/* 259 */       p.put("FECHA_INI", XDate.parse(p.get("FECHA_INI").toString(), "dd/MM/yyyy"));
/*     */     }
/* 261 */     XDate.setDefaultFormat("dd/MM/yyyy");
/* 262 */     List<Warrant> data = this.warrantFacade.load(0, 0, null, p);
/*     */     
/* 264 */     if (p.containsKey("FECHA_FIN")) {
/* 265 */       p.put("FECHA_FIN", XDate.format((Date)p.get("FECHA_FIN")));
/*     */     }
/* 267 */     if (p.containsKey("FECHA_INI")) {
/* 268 */       p.put("FECHA_INI", XDate.format((Date)p.get("FECHA_INI")));
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 274 */     p.put("IS_ONE_PAGE_PER_SHEET", Boolean.valueOf(false));
/* 275 */     p.put("SIGN_SECTION", Boolean.valueOf(true));
/* 276 */     p.put(DataSource.class, data);
/* 277 */     int opt = XUtil.intValue(m.get("option"));
/*     */     
/* 279 */     switch (opt) {
/*     */       case 1:
/* 281 */         jr = "cartaFianza_1";
/*     */         break;
/*     */       default:
/* 284 */         jr = "cartaFianza"; break;
/*     */     } 
/* 286 */     switch (XUtil.intValue(m.get("group"))) {
/*     */       case 1:
/* 288 */         jr = "cartaFianza_x_expediente";
/*     */         break;
/*     */       case 2:
/* 291 */         jr = "cartaFianza_x_proveedor"; break;
/*     */     } 
/* 293 */     String jr = "/gob/regionancash/tesoreria/jr/" + jr + ".jasper";
/* 294 */     p.put("rest", Boolean.valueOf(true));
/* 295 */     Object o = JR.open(jr, p);
/* 296 */     String filename = "";
/* 297 */     return 
/* 298 */       Response.ok(o, "application/octet-stream")
/* 299 */       .header("content-disposition", "attachment; filename = " + filename)
/* 300 */       .build();
/*     */   }
/*     */   
/*     */   public boolean initPage(Map<String, Date> m) {
/* 304 */     if (!m.containsKey("init")) {
/* 305 */       Date d = new Date();
/* 306 */       Calendar c = Calendar.getInstance();
/* 307 */       c.setTime(d);
/* 308 */       c.set(5, 1);
/* 309 */       m.put("FECHA_INI", c.getTime());
/* 310 */       c.setTimeInMillis(XDate.getEndOfMonth(d.getTime()));
/* 311 */       m.put("FECHA_FIN", c.getTime());
/* 312 */       m.put("TITLE_REPORT", "REPORTE DE CARTAS FIANZAS");
/* 313 */       m.put("init", Boolean.valueOf(true));
/*     */     } 
/* 315 */     return true;
/*     */   }
/*     */ }


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/rest/WarrantFacadeREST.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */