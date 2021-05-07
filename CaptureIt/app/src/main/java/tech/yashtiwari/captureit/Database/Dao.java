package tech.yashtiwari.captureit.Database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@androidx.room.Dao
public interface Dao {

    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Product product);

    @Query("DELETE FROM product_table")
    void deleteAll();

    @Query("SELECT * from product_table ")
    LiveData<List<Product>> getProducts();
}