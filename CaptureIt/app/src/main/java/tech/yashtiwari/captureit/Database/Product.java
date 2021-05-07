package tech.yashtiwari.captureit.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "product_table")
public class Product {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    public void setMProduct(@NonNull String mProduct) {
        this.mProduct = mProduct;
    }

    @NonNull
    @ColumnInfo(name = "product")
    private String mProduct;

    public Product(@NonNull String product) {
        this.mProduct = product;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMProduct(){return this.mProduct;}

    public int getId() {
        return this.id;
    }
}
