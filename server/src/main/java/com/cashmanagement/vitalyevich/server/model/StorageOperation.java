package com.cashmanagement.vitalyevich.server.model;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "storage_operations")
public class StorageOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    @Column(name = "update_date", nullable = false)
    private LocalDate updateDate;

    @Column(name = "amount_operation", nullable = false)
    private Integer amountOperation;

    public Integer getAmountOperation() {
        return amountOperation;
    }

    public void setAmountOperation(Integer amountOperation) {
        this.amountOperation = amountOperation;
    }

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StorageOperation(Integer id, Storage storage, LocalDate updateDate, Integer amountOperation) {
        this.id = id;
        this.storage = storage;
        this.updateDate = updateDate;
        this.amountOperation = amountOperation;
    }

    public StorageOperation(Storage storage, LocalDate updateDate, Integer amountOperation) {
        this.storage = storage;
        this.updateDate = updateDate;
        this.amountOperation = amountOperation;
    }

    public StorageOperation() {
    }
}