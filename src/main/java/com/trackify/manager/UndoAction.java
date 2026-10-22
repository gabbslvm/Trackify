package com.trackify.manager;

import com.trackify.dao.TransactionDAO;
import com.trackify.model.Transaction;

public abstract class UndoAction {
    protected final Transaction data;

    protected UndoAction(Transaction data) {
        this.data = data;
    }

    public abstract void undo(TransactionDAO dao, int userId);

    public static class Add extends UndoAction {
        public Add(Transaction t) {
            super(t);
        }

        @Override
        public void undo(TransactionDAO dao, int userId) {
            dao.deleteTransaction(data.getId(), userId);
        }
    }

    public static class Edit extends UndoAction {
        public Edit(Transaction oldValue) {
            super(oldValue);
        }

        @Override
        public void undo(TransactionDAO dao, int userId) {
            dao.updateTransaction(data);
        }
    }

    public static class Delete extends UndoAction {
        public Delete(Transaction deleted) {
            super(deleted);
        }

        @Override
        public void undo(TransactionDAO dao, int userId) {
            dao.restoreTransaction(data);
        }
    }
}