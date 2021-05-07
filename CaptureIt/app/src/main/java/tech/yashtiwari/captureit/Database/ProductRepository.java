package tech.yashtiwari.captureit.Database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ProductRepository {


    private Dao dao;
    private LiveData<List<Product>> mAllProducts;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    ProductRepository(Application application) {
        ProductRoomDatabase db = ProductRoomDatabase.getDatabase(application);
        dao = db.dao();
        mAllProducts = dao.getProducts();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Product>> getmAllProducts() {
        return mAllProducts;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Product word) {
        ProductRoomDatabase.databaseWriteExecutor.execute(() -> {
            dao.insert(word);
        });
    }

}
