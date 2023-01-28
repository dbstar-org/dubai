package io.github.dbstarll.dubai.model.collection;

public class CollectionInitializeException extends RuntimeException {
    private static final long serialVersionUID = -6296249399787627842L;

    /**
     * 构造一个集合初始化异常.
     *
     * @param message 异常消息
     */
    public CollectionInitializeException(final String message) {
        super(message);
    }
}
