package WEB-INF.classes.gob.regionancash.tesoreria.ejb;

import gob.regionancash.tesoreria.jpa.Warrant;
import gob.regionancash.tesoreria.jpa.WarrantType;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface WarrantFacadeLocal extends AbstractFacadeLocal {
  int getMaxExpediente();
  
  List<Warrant> load(int paramInt1, int paramInt2, String paramString, Map<String, Object> paramMap);
  
  void create(Warrant paramWarrant);
  
  void edit(Warrant paramWarrant);
  
  void remove(Warrant paramWarrant);
  
  Warrant find(Object paramObject);
  
  List<Warrant> findAll();
  
  List<Warrant> findRange(int[] paramArrayOfint);
  
  long count();
  
  void remove(List<Warrant> paramList);
  
  List<WarrantType> getWarrantTypeList();
  
  Object getContent(Map paramMap);
}


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/ejb/WarrantFacadeLocal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */