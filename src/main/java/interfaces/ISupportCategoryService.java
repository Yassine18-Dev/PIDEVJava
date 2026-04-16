package interfaces;

import entities.SupportCategory;
import java.util.List;

public interface ISupportCategoryService {
    void add(SupportCategory category);
    List<SupportCategory> getAll();
}