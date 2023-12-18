package io.github.sekassel.uno;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class Returnable<T> {

    private final ReturnStatus status;
    private final T value;

    public Returnable(ReturnStatus status, T value) {
        if (value == null && status.isSuccessful())
            throw new IllegalArgumentException("Status is successful even though value is null");

        this.status = status;
        this.value = value;
    }

    public T getValue() {
        if (value == null)
            throw new NoSuchElementException("No value present");
        return this.value;
    }

    public T getValueOrElse(T value) {
        if (this.value == null)
            return value;
        return this.value;
    }

    public boolean isSuccessful() {
        return this.status.isSuccessful();
    }

    public ReturnStatus getStatus() {
        return this.status;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Returnable))
            return false;

        Returnable<?> returnable = (Returnable) other;

        return this.getStatus().equals(returnable.getStatus()) && this.getValue().equals(returnable.getValue());
    }

    @Override
    public String toString() {
        return "[%s %s]".formatted(this.status, this.value);
    }


    public static class ReturnStatus {

        private static final HashMap<Integer, ReturnStatus> VALUES = new HashMap();

        public static final ReturnStatus SUCCESS = ReturnStatus.of(true, 0).register();
        public static final ReturnStatus ERROR_USERNAME_PASSWORD = ReturnStatus.of(true, 1).register();
        public static final ReturnStatus ERROR_NAME_TAKEN = ReturnStatus.of(true, 2).register();

        private boolean success;
        private int code;

        private ReturnStatus(boolean success, int code) {
            this.success = success;
            this.code = code;
        }

        public static ReturnStatus of(boolean success, int code) {
            return new ReturnStatus(success, code);
        }

        public ReturnStatus register() {
            if (VALUES.containsKey(this.getCode()))
                throw new IllegalArgumentException("Cannot register status as %s is already in use".formatted(this.getCode()));
            VALUES.put(this.getCode(), this);
            return this;
        }

        public int getCode() {
            return this.code;
        }

        public boolean isSuccessful() {
            return this.success;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Returnable.ReturnStatus))
                return false;
            return ((ReturnStatus) other).getCode() == this.getCode();
        }

        @Override
        public String toString() {
            return "%s: %s".formatted(this.getCode(), this.isSuccessful());
        }
    }

}
