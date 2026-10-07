/*     */ package WEB-INF.classes.gob.regionancash.tesoreria.ejb;
/*     */ 
/*     */ import gob.regionancash.tesoreria.ejb.TesoreriaFacade;
/*     */ import gob.regionancash.tesoreria.ejb.WarrantFacadeLocal;
/*     */ import gob.regionancash.tesoreria.jpa.Warrant;
/*     */ import gob.regionancash.tesoreria.jpa.WarrantType;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.annotation.PostConstruct;
/*     */ import javax.ejb.EJB;
/*     */ import javax.ejb.Stateless;
/*     */ import javax.persistence.EntityManager;
/*     */ import javax.persistence.Query;
/*     */ import javax.servlet.http.HttpServletRequest;
/*     */ import javax.sql.DataSource;
/*     */ import org.isobit.app.Notifiable;
/*     */ import org.isobit.app.X;
/*     */ import org.isobit.app.ejb.BlockFacadeLocal;
/*     */ import org.isobit.app.ejb.SessionFacadeLocal;
/*     */ import org.isobit.app.ejb.UserFacadeLocal;
/*     */ import org.isobit.jreport.JR;
/*     */ import org.isobit.util.AbstractFacade;
/*     */ import org.isobit.util.XDate;
/*     */ import org.isobit.util.XMap;
/*     */ import org.isobit.util.XUtil;
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
/*     */ @Stateless
/*     */ public class WarrantFacade
/*     */   extends AbstractFacade<Warrant>
/*     */   implements WarrantFacadeLocal, BlockFacadeLocal.BlockModule, Notifiable
/*     */ {
/*     */   @EJB
/*     */   private SessionFacadeLocal sessionFacade;
/*     */   @EJB
/*     */   private UserFacadeLocal userFacade;
/*     */   
/*     */   public List<WarrantType> getWarrantTypeList() {
/*  53 */     return getEntityManager().createQuery("SELECT t FROM WarrantType t ORDER BY t.name").getResultList();
/*     */   }
/*     */   
/*     */   public Object getContent(Map<?, ?> m) {
/*     */     String jr;
/*  58 */     Map<Object, Object> p = new HashMap<>();
/*  59 */     p.putAll(m);
/*  60 */     if (p.containsKey("FORMAT")) {
/*  61 */       p.put(JR.EXTENSION, p.get("FORMAT"));
/*     */     }
/*     */     
/*  64 */     XDate.setDefaultFormat("dd/MM/yyyy");
/*     */     
/*  66 */     if (p.containsKey("FECHA_FIN")) {
/*  67 */       p.put("FECHA_FIN", XDate.format((Date)p.get("FECHA_FIN")));
/*     */     }
/*  69 */     if (p.containsKey("FECHA_INI")) {
/*  70 */       p.put("FECHA_INI", XDate.format((Date)p.get("FECHA_INI")));
/*     */     }
/*  72 */     System.out.println(p);
/*     */ 
/*     */ 
/*     */     
/*  76 */     p.put("IS_ONE_PAGE_PER_SHEET", Boolean.valueOf(false));
/*  77 */     List<Warrant> data = load(0, 0, null, (Map)p);
/*  78 */     p.put("SIGN_SECTION", Boolean.valueOf(true));
/*  79 */     p.put(DataSource.class, data);
/*  80 */     int opt = XUtil.intValue(m.get("option"));
/*     */     
/*  82 */     switch (opt) {
/*     */       case 1:
/*  84 */         jr = "cartaFianza_1";
/*     */         break;
/*     */       default:
/*  87 */         jr = "cartaFianza"; break;
/*     */     } 
/*  89 */     switch (XUtil.intValue(m.get("group"))) {
/*     */       case 1:
/*  91 */         jr = "cartaFianza_x_expediente";
/*     */         break;
/*     */       case 2:
/*  94 */         jr = "cartaFianza_x_proveedor"; break;
/*     */     } 
/*  96 */     return JR.open("/gob/regionancash/tesoreria/jr/" + jr + ".jasper", p);
/*     */   }
/*     */ 
/*     */   
/*     */   public int getMaxExpediente() {
/* 101 */     return XUtil.intValue(getEntityManager().createQuery("SELECT MAX(w.expediente) FROM Warrant w").getSingleResult());
/*     */   }
/*     */ 
/*     */   
/*     */   public List<Warrant> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
/* 106 */     String order = (String)filters.get("order");
/* 107 */     if (order == null) {
/* 108 */       order = "";
/*     */     }
/* 110 */     String carta = (String)XUtil.isEmpty(filters.get("code"), null);
/* 111 */     String obra = (String)XUtil.isEmpty(filters.get("obra"), null);
/* 112 */     Date[] renovacion = XDate.getPeriod((String)filters.get("renovacion"));
/* 113 */     String provider = (String)XUtil.isEmpty(filters.get("provider"), null);
/* 114 */     Object expediente = XUtil.isEmpty(filters.get("expediente"), null);
/* 115 */     int faltan = XUtil.intValue(filters.get("faltan"));
/* 116 */     boolean danger = XUtil.booleanValue(filters.get("danger"));
/* 117 */     if (danger) {
/* 118 */       faltan = 1;
/*     */     }
/* 120 */     Object entidad = XUtil.isEmpty(filters.get("entidad"), null);
/* 121 */     Object<Integer> warrantType = (Object<Integer>)XUtil.isEmpty(filters.get("warrantType"), null);
/* 122 */     Date fecha_ini = (Date)filters.get("FECHA_INI");
/* 123 */     Date fecha_fin = (Date)filters.get("FECHA_FIN");
/* 124 */     Date[] vencimiento = XDate.getPeriod((String)filters.get("vencimiento"));
/* 125 */     if (vencimiento != null) {
/* 126 */       fecha_ini = vencimiento[0];
/* 127 */       fecha_fin = vencimiento[1];
/*     */     } 
/* 129 */     List<Query> ql = new ArrayList<>();
/*     */     
/* 131 */     EntityManager em = getEntityManager();
/*     */     String sql;
/* 133 */     ql.add(em.createQuery("SELECT w " + (sql = "FROM Warrant w WHERE w.canceled=0 " + ((expediente != null) ? " AND CONCAT(substr('000',length(w.expediente)),w.expediente) like :expediente" : "") + ((entidad != null) ? " AND UPPER(w.entidad) like :entidad" : "") + ((obra != null) ? " AND UPPER(w.obra) like :obra" : "") + ((carta != null) ? " AND UPPER(w.nroCarta) like :carta" : "") + ((faltan != 0) ? ((faltan > 0) ? " AND (function('DAYS_DIFF',:hoy,w.fechaVencimiento)>0 AND function('DAYS_DIFF',:hoy,w.fechaVencimiento)<=5)" : " AND function('DAYS_DIFF',:hoy,w.fechaVencimiento)<=0 ") : "") + ((provider != null) ? " AND UPPER(w.proveedor) like :proveedor" : "") + ((warrantType != null) ? " AND w.warrantType.id IN :warrantType" : "") + ((renovacion != null) ? (((renovacion[0] != null) ? " AND DATE(w.fechaRenovacion)>=:renovacion_ini" : "") + ((renovacion[1] != null) ? " AND DATE(w.fechaRenovacion)<=:renovacion_fin" : "")) : "") + ((fecha_ini != null) ? " AND DATE(w.fechaVencimiento)>=:fecha_ini" : "") + ((fecha_fin != null) ? " AND DATE(w.fechaVencimiento)<=:fecha_fin" : "")) + "  ORDER BY " + (
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
/*     */ 
/*     */ 
/*     */           
/* 151 */           order.equals("e") ? "w.expediente DESC," : "") + "w.fechaVencimiento DESC"));
/* 152 */     Date today = X.getServerDate();
/* 153 */     double i = 8.64E7D;
/*     */     
/* 155 */     if (pageSize > 0) {
/* 156 */       ((Query)ql.get(0)).setFirstResult(first).setMaxResults(pageSize);
/* 157 */       ql.add(em.createQuery("SELECT COUNT(w) " + sql));
/*     */     } 
/* 159 */     if (warrantType != null) {
/* 160 */       List<Integer> list = new ArrayList();
/* 161 */       for (Object o : (Object[])warrantType) {
/* 162 */         list.add(Integer.valueOf(Integer.parseInt(o.toString())));
/*     */       }
/* 164 */       warrantType = (Object<Integer>)list;
/*     */     } 
/*     */     
/* 167 */     for (Query q : ql) {
/* 168 */       if (faltan != 0) {
/* 169 */         q.setParameter("hoy", new Date());
/*     */       }
/* 171 */       if (renovacion != null) {
/* 172 */         if (renovacion[0] != null) {
/* 173 */           q.setParameter("renovacion_ini", renovacion[0]);
/*     */         }
/* 175 */         if (renovacion[1] != null) {
/* 176 */           q.setParameter("renovacion_fin", renovacion[1]);
/*     */         }
/*     */       } 
/* 179 */       if (fecha_ini != null) {
/* 180 */         q.setParameter("fecha_ini", fecha_ini);
/*     */       }
/* 182 */       if (fecha_fin != null) {
/* 183 */         q.setParameter("fecha_fin", fecha_fin);
/*     */       }
/* 185 */       if (expediente != null) {
/* 186 */         q.setParameter("expediente", "%" + expediente.toString().replace(" ", "%") + "%");
/*     */       }
/* 188 */       if (carta != null) {
/* 189 */         q.setParameter("carta", "%" + carta.replace(" ", "%") + "%");
/*     */       }
/* 191 */       if (warrantType != null) {
/* 192 */         q.setParameter("warrantType", warrantType);
/*     */       }
/* 194 */       if (entidad != null) {
/* 195 */         q.setParameter("entidad", "%" + entidad.toString().toUpperCase().replace(" ", "%") + "%");
/*     */       }
/* 197 */       if (obra != null) {
/* 198 */         q.setParameter("obra", "%" + obra.toUpperCase().replace(" ", "%") + "%");
/*     */       }
/* 200 */       if (provider != null) {
/* 201 */         q.setParameter("proveedor", "%" + provider.toUpperCase().replace(" ", "%") + "%");
/*     */       }
/*     */     } 
/* 204 */     if (pageSize > 0) {
/* 205 */       filters.put("size", ((Query)ql.get(1)).getSingleResult());
/*     */     }
/*     */     
/* 208 */     List<Warrant> l = ((Query)ql.get(0)).getResultList();
/* 209 */     List<Warrant> l2 = danger ? new ArrayList() : l;
/* 210 */     for (Warrant w : l) {
/* 211 */       if (w.getFechaVencimiento() != null) {
/* 212 */         double i2 = (w.getFechaVencimiento().getTime() - today.getTime()) / i;
/*     */         
/* 214 */         i2 = Math.ceil(i2);
/* 215 */         w.setDiff(Double.valueOf(i2));
/* 216 */         if (danger && i2 > 0.0D && i2 <= 5.0D) {
/* 217 */           l2.add(w);
/*     */         }
/*     */       } 
/*     */     } 
/* 221 */     return l2;
/*     */   }
/*     */ 
/*     */   
/*     */   public void edit(Warrant entity) {
/* 226 */     if (entity.getFechaRegistro() == null) {
/* 227 */       entity.setFechaRegistro(X.getServerDate());
/*     */     }
/* 229 */     if (entity.getId() == null) {
/* 230 */       int expediente = XUtil.intValue(entity.getExpediente());
/* 231 */       if (expediente == 0);
/*     */ 
/*     */       
/* 234 */       int numero = XUtil.intValue(getEntityManager().createQuery("SELECT MAX(w.numero) FROM Warrant w WHERE w.expediente=:expediente")
/* 235 */           .setParameter("expediente", Integer.valueOf(expediente)).getSingleResult()) + 1;
/* 236 */       entity.setNumero(Integer.valueOf(numero));
/* 237 */       create(entity);
/* 238 */       getEntityManager().createQuery("UPDATE Warrant w SET w.renovated=1 WHERE w.expediente=:expediente AND w.warrantType=:warrantType AND w.fechaVencimiento<:fechaVencimiento AND function('DAYS_DIFF',:hoy,w.fechaVencimiento)>0")
/* 239 */         .setParameter("expediente", entity.getExpediente())
/* 240 */         .setParameter("warrantType", entity.getWarrantType())
/* 241 */         .setParameter("fechaVencimiento", entity.getFechaVencimiento())
/* 242 */         .setParameter("hoy", X.getServerDate())
/* 243 */         .executeUpdate();
/*     */     } else {
/* 245 */       super.edit(entity);
/*     */     } 
/* 247 */     if (!XUtil.isEmpty(entity.getProcessType())) {
/* 248 */       getEntityManager().createQuery("UPDATE Warrant w SET w.processType=:processType WHERE w.expediente=:expediente AND w.obra=:obra")
/* 249 */         .setParameter("processType", entity.getProcessType())
/* 250 */         .setParameter("obra", entity.getObra())
/* 251 */         .setParameter("expediente", entity.getExpediente()).executeUpdate();
/*     */     }
/*     */   }
/*     */   
/*     */   @PostConstruct
/*     */   public void init() {
/* 257 */     add(this);
/*     */   }
/*     */ 
/*     */   
/*     */   public Object getBlock(HttpServletRequest request, String op, Object delta) {
/* 262 */     if ("list".equals(op)) {
/* 263 */       Map<String, XMap> blocks = (Map)request.getAttribute("#blocks");
/* 264 */       blocks.put("notifications", new XMap(new Object[] { "info", "Notifications" }));
/* 265 */       return blocks;
/* 266 */     }  if ("view".equals(op) && 
/* 267 */       this.userFacade.access(TesoreriaFacade.Perm.ACCESS_TESORERIA)) {
/* 268 */       String[] q = (String[])request.getAttribute("#q");
/* 269 */       if (q.length <= 2 || !"cartaFianza".equals(q[2]))
/*     */       {
/* 271 */         return new XMap(new Object[] { "title", "Notifications", "src", "/cartaFianza/Notifications.xhtml" });
/*     */       }
/*     */     } 
/*     */     
/* 275 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Object getItem() {
/* 281 */     if (this.userFacade.access(TesoreriaFacade.Perm.ACCESS_TESORERIA)) {
/* 282 */       Object n = this.sessionFacade.get("tesoreriaNotify");
/* 283 */       if (n == null) {
/* 284 */         EntityManager em = getEntityManager();
/* 285 */         int c = XUtil.intValue(em.createQuery("SELECT count(e) from Warrant e WHERE :today-e.fechaVencimiento<=5")
/* 286 */             .setParameter("today", X.getServerDate())
/* 287 */             .getSingleResult());
/* 288 */         (new Object[2])[0] = "hay " + c + " cartas fianzas por vencer"; (new Object[2])[1] = "/admin/tesoreria/cartaFianza"; this.sessionFacade.put("tesoreriaNotify", (c > 0) ? new Object[2] : Boolean.FALSE);
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 295 */       return (n == Boolean.FALSE) ? null : n;
/*     */     } 
/* 297 */     return null;
/*     */   }
/*     */ }


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/ejb/WarrantFacade.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */