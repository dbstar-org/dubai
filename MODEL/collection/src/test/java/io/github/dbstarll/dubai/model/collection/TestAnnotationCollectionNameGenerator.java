package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.Namespace;
import io.github.dbstarll.dubai.model.entity.Table;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestAnnotationCollectionNameGenerator {
    private CollectionNameGenerator collectionNameGenerator;

    @BeforeEach
    void setUp() {
        this.collectionNameGenerator = new AnnotationCollectionNameGenerator();
    }

    @AfterEach
    void tearDown() {
        this.collectionNameGenerator = null;
    }

    @Test
    void testSingleWord() {
        assertEquals("one", collectionNameGenerator.generateCollectionName(One.class));
    }

    @Test
    void testTwoWord() {
        assertEquals("two_two", collectionNameGenerator.generateCollectionName(TwoTwo.class));
    }

    @Test
    void testInheritedSchema() {
        assertEquals("t1_abc", collectionNameGenerator.generateCollectionName(Three.class));
    }

    @Test
    void testDirectSchema() {
        assertEquals("t2_four", collectionNameGenerator.generateCollectionName(Four.class));
    }

    @Test
    void testClassSchema() {
        assertEquals("t3_five", collectionNameGenerator.generateCollectionName(Five.class));
    }

    @Test
    void testEmptySchema() {
        assertEquals("six", collectionNameGenerator.generateCollectionName(Six.class));
    }

    /**
     * 测试抛出异常.
     */
    @Test
    void testNoTable() {
        try {
            collectionNameGenerator.generateCollectionName(NoTable.class);
        } catch (CollectionInitializeException ex) {
            assertTrue(ex.getMessage().startsWith("Table annotation not find on entity class: "));
        }
    }

    @Table
    public interface One extends Entity {
    }

    @Table
    public interface TwoTwo extends Entity {
    }

    @Namespace("t1")
    public interface NoTable extends Entity {
    }

    @Table("abc")
    public interface Three extends NoTable {
    }

    @Table
    @Namespace("t2_")
    public interface Four extends NoTable {
    }

    @Namespace("t3_")
    public static class ClassEntity implements Entity {
        private static final long serialVersionUID = -2156207737656566869L;

        @Override
        public ObjectId getId() {
            return null;
        }

        @Override
        public Date getDateCreated() {
            return null;
        }

        @Override
        public Date getLastModified() {
            return null;
        }
    }

    @Table
    public static class Five extends ClassEntity {
        private static final long serialVersionUID = 5920039216901835804L;
    }

    @Table
    @Namespace
    public interface Six extends Entity {
    }
}
