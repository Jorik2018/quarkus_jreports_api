package WEB-INF.classes.gob.regionancash.tesoreria.ejb;

import gob.regionancash.tesoreria.jpa.WarrantType;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface WarrantTypeFacadeLocal extends AbstractFacadeLocal {
  List<WarrantType> load(int paramInt1, int paramInt2, String paramString, Map<String, Object> paramMap);
  
  void create(WarrantType paramWarrantType);
  
  void edit(WarrantType paramWarrantType);
  
  void remove(WarrantType paramWarrantType);
  
  WarrantType find(Object paramObject);
  
  List<WarrantType> findAll();
  
  List<WarrantType> findRange(int[] paramArrayOfint);
  
  long count();
  
  void remove(List<WarrantType> paramList);
}


/* Location:              /Users/ealarcop/Downloads/tesoreria_web_admin.war!/WEB-INF/classes/gob/regionancash/tesoreria/ejb/WarrantTypeFacadeLocal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */