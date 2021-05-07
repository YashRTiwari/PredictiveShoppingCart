package tech.yashtiwari.captureit.Database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {


    private ProductRepository mRepository;

    private LiveData<List<Product>> allProducts;

    public ProductViewModel(Application application) {
        super(application);
        mRepository = new ProductRepository(application);
        allProducts = mRepository.getmAllProducts();
    }

    LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public void insert(Product word) {
        mRepository.insert(word);
    }

}
