package de.jonas_kraus.learn_app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Data.MarkedQuestion;

/**
 * Created by Jonas on 01.03.2015.
 */
public class DbManager {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allQuestionColumns = {
            MySQLiteHelper.COLUMN_QUESTION_ID,          //0
            MySQLiteHelper.COLUMN_QUESTION_TYPE,        //1
            MySQLiteHelper.COLUMN_QUESTION_QUESTION,    //2
            MySQLiteHelper.COLUMN_QUESTION_KNOWN,       //3
            MySQLiteHelper.COLUMN_QUESTION_RATING,      //4
            MySQLiteHelper.COLUMN_QUESTION_HINT,        //5
            MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID, //6
            MySQLiteHelper.COLUMN_QUESTION_DRAWER,      //7
            MySQLiteHelper.COLUMN_QUESTION_MARKED,      //8
            MySQLiteHelper.COLUMN_QUESTION_DATE,        //9
            MySQLiteHelper.COLUMN_QUESTION_VIEWED       //10
    };
    private String[] allAnswerColumns = {
            MySQLiteHelper.COLUMN_ANSWER_ID,
            MySQLiteHelper.COLUMN_ANSWER_QUESTION_ID,
            MySQLiteHelper.COLUMN_ANSWER_IS_CORRECT,
            MySQLiteHelper.COLUMN_ANSWER_ANSWER
    };
    private String[] allCategoryColumns = {
            MySQLiteHelper.COLUMN_CATEGORY_ID,
            MySQLiteHelper.COLUMN_CATEGORY_NAME,
            MySQLiteHelper.COLUMN_CATEGORY_PARENT,
            MySQLiteHelper.COLUMN_CATEGORY_MARKED
    };

    private String[] allMarkColumns = {
            MySQLiteHelper.COLUMN_MARKED_ID,
            MySQLiteHelper.COLUMN_QUESTION_ID
    };

    private String[] allSettingsColumns = {
            MySQLiteHelper.COLUMN_SETTINGS_SHOW_HINT,
            MySQLiteHelper.COLUMN_SETTINGS_SHOW_BAR,
            MySQLiteHelper.COLUMN_SETTINGS_MULTIPLECHOICE_CHANGE_ANSWER_ORDER,
            MySQLiteHelper.COLUMN_SETTINGS_VIEW_RANDOM_CARDS,
            MySQLiteHelper.COLUMN_SETTINGS_VIEW_LAST_DRAWER_CARDS,
            MySQLiteHelper.COLUMN_SETTINGS_NIGHT_MODE,
            MySQLiteHelper.COLUMN_SETTINGS_TEXTSIZE_QUESTIONS,
            MySQLiteHelper.COLUMN_SETTINGS_TEXTSIZE_ANSWERS,
            MySQLiteHelper.COLUMN_SETTINGS_CARDS_ORDER,
    };

    private String[] allStatisticsColumns = {
            MySQLiteHelper.COLUMN_STATISTICS_ID,
            MySQLiteHelper.COLUMN_STATISTICS_PARENT_ID,
            MySQLiteHelper.COLUMN_STATISTICS_NUM_CARDS,
            MySQLiteHelper.COLUMN_STATISTICS_NUM_KNOWN,
            MySQLiteHelper.COLUMN_STATISTICS_NUM_NOT_KNOWN,
            MySQLiteHelper.COLUMN_STATISTICS_NUM_VIEWED,
            MySQLiteHelper.COLUMN_STATISTICS_NUM_NOT_VIEWED,
            MySQLiteHelper.COLUMN_STATISTICS_TIME
    };

    public DbManager(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    Date date = new java.util.Date();

    /**
     * Creating a card with answers
     * @param card
     */
    public Card createCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_QUESTION, card.getQuestion());
        values.put(MySQLiteHelper.COLUMN_QUESTION_TYPE, card.getType()+"");
        values.put(MySQLiteHelper.COLUMN_QUESTION_KNOWN, card.isKnown());
        values.put(MySQLiteHelper.COLUMN_QUESTION_RATING, card.getRating());
        values.put(MySQLiteHelper.COLUMN_QUESTION_HINT, card.getHint());
        values.put(MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID, card.getCategoryId());
        long insertId = database.insert(MySQLiteHelper.TABLE_QUESTIONS,null,values);
        /*
        database.execSQL("INSERT INTO " + MySQLiteHelper.TABLE_QUESTIONS + " ("
                        + MySQLiteHelper.COLUMN_QUESTION_TYPE + ", "
                        + MySQLiteHelper.COLUMN_QUESTION_QUESTION + " ,"
                        + MySQLiteHelper.COLUMN_QUESTION_KNOWN + " ,"
                        + MySQLiteHelper.COLUMN_QUESTION_RATING + " ) "
                        + "VALUES("
                        + card.getType() + ", "
                        + card.getQuestion() + ", "
                        + card.isKnown() + " ,"
                        + card.getRating() + " )"
        );
        */
        createAnswer(insertId, card.getAnswers());
        card.setId((int)insertId);
        return card;
    }

    /**
     * Creates cards from a given list
     * @param cards
     * @param id
     */
    public void createCards(List<Card> cards, int id) {
        for (Card card : cards) {
            card.setCategoryId(id);
            createCard(card);
        }
    }

    /**
     * Creating all given answers
     * @param questionId
     * @param answers
     */
    private void createAnswer(long questionId, List<Answer> answers) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ANSWER_QUESTION_ID, questionId);
        for (Answer answer : answers) {
            answer.setQuestionId((int) questionId);
            values.put(MySQLiteHelper.COLUMN_ANSWER_IS_CORRECT, answer.isCorrect());
            values.put(MySQLiteHelper.COLUMN_ANSWER_ANSWER, answer.getAnswer());
            database.insert(MySQLiteHelper.TABLE_ANSWERS, null, values);
        }
    }

    /**
     * Creates a Category and returns its inserted id
     * @param category
     * @return category
     */
    public Category createCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CATEGORY_PARENT, category.getParentId());
        values.put(MySQLiteHelper.COLUMN_CATEGORY_NAME, category.getName());
        category.setId((int) database.insert(MySQLiteHelper.TABLE_CATEGORIES, null, values));
        return category;
    }

    /**
     * Collects all Cards for given category
     * @param category
     * @return
     */
    public List<Card> getCards(Category category) {
        List<Card> cards = new ArrayList<Card>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID + " = " + category.getId(), null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                String hint = cursor.getString(5);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                List<Answer> answers = getAnswers(id);
                cards.add(new Card(id, Card.CardType.valueOf(type), question, answers, known, rating, hint, category.getId(), drawer, marked, date, viewed));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }

    /**
     * Collects all Cards for given category
     * @param id
     * @return
     */
    public List<Card> getCards(Integer id) {
        List<Card> cards = new ArrayList<Card>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID + " = " + id, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                String hint = cursor.getString(5);
                List<Answer> answers = getAnswers(id);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                cards.add(new Card(_id, Card.CardType.valueOf(type), question, answers, known, rating, hint, id, drawer, marked, date, viewed));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }

    /**
     * Collects all Cards randomized
     * @return
     */
    public List<Card> getAllCardsRandomized() {
        List<Card> cards = new ArrayList<Card>();
        Cursor cursor;
        if (!isViewCardsOfLastDrawer()) {
            cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_DRAWER + " < " + 5, null, null, null, null);
        } else {
            cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS,allQuestionColumns ,null, null, null, null, null);
        }
        if (cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                String hint = cursor.getString(5);
                List<Answer> answers = getAnswers(_id);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                cards.add(new Card(_id, Card.CardType.valueOf(type), question, answers, known, rating, hint, _id, drawer, marked, date, viewed));
            } while (cursor.moveToNext());
        }
        cursor.close();
        long seed = System.nanoTime();
        Collections.shuffle(cards, new Random(seed));
        return cards;
    }

    public List<Card> getAllCardDescendantsFromCatalogues(ArrayList<Catalogue> catalogues) {
        /*
        List<Card> cards = new ArrayList<Card>();
        ArrayList<Category> parentCategories = new ArrayList<Category>();
        for (Catalogue curCatalogue : catalogues) {
            if (curCatalogue.getCategory() != null) {
                parentCategories.add(curCatalogue.getCategory());
            } else {
                cards.add(curCatalogue.getCard());
            }
        }
        ArrayList<Category> finalCats = getAllCategoryChildren(null,parentCategories);
        for (Category curCat : finalCats) {
            cards.addAll(getCards(curCat));
        }
        return cards;
        */
        List<Card> cards = new ArrayList<Card>();
        ArrayList<Integer> parentCategories = new ArrayList<Integer>();
        for (Catalogue curCatalogue : catalogues) {
            if (curCatalogue.getCategory() != null) {
                parentCategories.add(curCatalogue.getCategory().getId());
            } else {
                cards.add(curCatalogue.getCard());
            }
        }
        ArrayList<Integer> finalCats = getAllCategoryChildrenById(null, parentCategories);
        for (Integer curCat : finalCats) {
            cards.addAll(getCards(curCat));
        }
        return cards;
    }

    /**
     * Collects all answers for the given question id
     * @param id
     * @return
     */
    private List<Answer> getAnswers(int id) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANSWERS,allAnswerColumns,MySQLiteHelper.COLUMN_ANSWER_QUESTION_ID + " = " + id,null,null,null,null);
        List<Answer>answers = new ArrayList<Answer>();
        if (cursor.moveToFirst()) {
            do {
                answers.add(new Answer(cursor.getInt(0), cursor.getInt(1),cursor.getInt(2) > 0, cursor.getString(3)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return answers;
    }

    /**
     * returns a category chain beginning with the leave
     * @param categoryName
     * @return
     */
    public ArrayList<Category> getCategoryChain(String categoryName) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CATEGORIES,allCategoryColumns,MySQLiteHelper.COLUMN_CATEGORY_NAME+"="+categoryName,null,null,null,null);
        Category category;
        int curParId = -1;
        ArrayList<Category> chain = new ArrayList<Category>();
        if (cursor.moveToFirst()) {
            category = new Category(cursor.getInt(0),cursor.getInt(1),cursor.getString(2), cursor.getInt(3)>0);
            chain.add(category);
            curParId = category.getParentId();
            while (curParId != -1) {
                Cursor c = database.query(MySQLiteHelper.TABLE_CATEGORIES,allAnswerColumns,MySQLiteHelper.COLUMN_CATEGORY_ID+"="+curParId,null,null,null,null);
                if (c.moveToFirst()) {
                    chain.add(new Category(c.getInt(0), c.getInt(1), c.getString(2), cursor.getInt(3)>0));
                    curParId = c.getInt(1);
                }
            }
            cursor.close();
        }
        cursor.close();
        return chain;
    }
    /**
     * returns a category chain beginning with the root
     * @param id
     * @return
     */
    public ArrayList<Category> getCategoryChildren(int id) {

        int curId = id;
        ArrayList<Category> chain = new ArrayList<Category>();
        Cursor c = database.query(MySQLiteHelper.TABLE_CATEGORIES,allCategoryColumns,MySQLiteHelper.COLUMN_CATEGORY_PARENT+"="+curId,null,null,null,null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            chain.add(new Category(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3)>0));
        }
        c.close();

        return chain;
    }

    /**
     * returns a category chain beginning with the root
     * @param id
     * @return
     */
    public ArrayList<Integer> getCategoryChildren(Integer id) {

        int curId = id;
        ArrayList<Integer> chain = new ArrayList<Integer>();
        Cursor c = database.query(MySQLiteHelper.TABLE_CATEGORIES,allCategoryColumns,MySQLiteHelper.COLUMN_CATEGORY_PARENT+"="+curId,null,null,null,null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            chain.add(c.getInt(0));
        }
        c.close();

        return chain;
    }

    public ArrayList<Category> getAllCategoryChildren(ArrayList<Category>finalCats, ArrayList<Category>categories) {
        if (finalCats == null) {
            finalCats = categories;
        }
        if (categories.isEmpty() || categories == null) {
            return finalCats;
        }
        ArrayList<Category>curCats = new ArrayList<Category>();
        for (Category cat : categories) {
            curCats = getCategoryChildren(cat.getId());
            finalCats.addAll(curCats);
        }
        if (curCats.size() == 0) {
            return finalCats;
        } else {
            getAllCategoryChildren(finalCats, curCats);
        }
        return null;
    }

    public ArrayList<Integer> getAllCategoryChildrenById(ArrayList<Integer>finalCats, ArrayList<Integer>categories) {
        if (finalCats == null) {
            finalCats = categories;
        }
        if (categories.isEmpty() || categories == null) {
            return finalCats;
        }
        ArrayList<Integer>curCats = new ArrayList<Integer>();
        for (Integer cat : categories) {
            curCats = getCategoryChildren(cat);
            finalCats.addAll(curCats);
        }
        if (curCats.size() == 0) {
            return finalCats;
        } else {
            getAllCategoryChildrenById(finalCats, curCats);
        }
        return null;
    }

    /**
     * Collects all root categories and returns them in a list
     * @return
     */
    public List<Category> getCategoriesByLevel(int level) {
        List<Category> categories = new ArrayList<Category>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_CATEGORIES, allCategoryColumns, MySQLiteHelper.COLUMN_CATEGORY_PARENT+"="+(level), null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Category category = new Category(cursor.getInt(0),cursor.getInt(2),cursor.getString(1), cursor.getInt(3)>0); /* @TODO change values */
            categories.add(category);
            cursor.moveToNext();
        }
        cursor.close();
        return categories;
    }

    public List<Card> getCardsByLevel(int level) {
        List<Card> cards = new ArrayList<Card>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID + " = " + level, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                List<Answer> answers = getAnswers(id);
                String hint = cursor.getString(5);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                cards.add(new Card(id, Card.CardType.valueOf(type), question, answers, known, rating, hint, level, drawer, marked, date, viewed));
                Log.d("by level ", cards.get(cards.size()-1).toString());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }

    /**
     * Returns the Parent directory for a given parent id
     * @param parentId
     * @return
     */
    public Category getParentCategory(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CATEGORIES, allCategoryColumns, MySQLiteHelper.COLUMN_CATEGORY_ID + "=" + (parentId), null, null, null, null);
        if (cursor.moveToFirst()) {
            Category category = new Category(cursor.getInt(0), cursor.getInt(2), cursor.getString(1), cursor.getInt(3)>0); /* @TODO change values */
            cursor.close();
            return category;
        }
        return null;
    }

    public boolean deleteCategoryWithSubCategoriesAndCards(Category category) {
        /* @TODO Hast to cascade delete all subcategories, questions and answers*/
        return true;
    }

    /**
     * Updates the name of a category
     * @param category
     */
    public void updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CATEGORY_NAME, category.getName());
        database.update(MySQLiteHelper.TABLE_CATEGORIES, values, MySQLiteHelper.COLUMN_CATEGORY_ID + "=" + category.getId(), null);
    }

    public Card updateEditCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_ID, card.getId());
        values.put(MySQLiteHelper.COLUMN_QUESTION_QUESTION, card.getQuestion());
        values.put(MySQLiteHelper.COLUMN_QUESTION_TYPE, card.getType()+"");
        values.put(MySQLiteHelper.COLUMN_QUESTION_KNOWN, false);
        values.put(MySQLiteHelper.COLUMN_QUESTION_RATING, 0);
        values.put(MySQLiteHelper.COLUMN_QUESTION_HINT, card.getHint());
        values.put(MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID, card.getCategoryId());
        values.put(MySQLiteHelper.COLUMN_QUESTION_DRAWER, card.getDrawer());
        values.put(MySQLiteHelper.COLUMN_QUESTION_MARKED, card.isMarked());
        String nullString = null;
        values.put(MySQLiteHelper.COLUMN_QUESTION_VIEWED, nullString);
        database.update(MySQLiteHelper.TABLE_QUESTIONS, values, MySQLiteHelper.COLUMN_QUESTION_ID + "=" + card.getId(), null);
        updateEditAnswer(card.getAnswers());
        return card;
    }

    private void updateEditAnswer(List<Answer> answers) {
        database.delete(MySQLiteHelper.TABLE_ANSWERS, MySQLiteHelper.COLUMN_ANSWER_QUESTION_ID + "=" + answers.get(0).getQuestionId(), null);
        createAnswer(answers.get(0).getQuestionId(), answers);
    }

    public void deleteCard(int id) {
        database.delete(MySQLiteHelper.TABLE_QUESTIONS, MySQLiteHelper.COLUMN_QUESTION_ID + "=" + id, null);
        deleteAnswers(id);
    }

    private void deleteAnswers(int questionId) {
        database.delete(MySQLiteHelper.TABLE_ANSWERS, MySQLiteHelper.COLUMN_ANSWER_QUESTION_ID + "=" + questionId, null);
    }

    public List<Card> getCardDescendantsFromCatalogues(ArrayList<Catalogue> checkedCatalogue) {
        List<Card> cards = new ArrayList<Card>();
        for (Catalogue curCatalogue : checkedCatalogue) {
            if (curCatalogue.getCategory() != null) {
                cards.addAll(getCards(curCatalogue.getCategory().getId()));
            } else {
                cards.add(curCatalogue.getCard());
            }
        }
        return cards;

    }

    public Card updateRating(int id, int rating, boolean known, int drawer) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_KNOWN, known);
        values.put(MySQLiteHelper.COLUMN_QUESTION_RATING, rating);
        //values.put(MySQLiteHelper.COLUMN_QUESTION_DATE, "DATETIME(now)");
        values.put(MySQLiteHelper.COLUMN_QUESTION_VIEWED, new Timestamp(date.getTime()).toString());
        database.update(MySQLiteHelper.TABLE_QUESTIONS, values, MySQLiteHelper.COLUMN_QUESTION_ID + "=" + id, null);
        values.clear();
        if (known) {
            values.put(MySQLiteHelper.COLUMN_QUESTION_DRAWER, drawer+1);
            database.update(MySQLiteHelper.TABLE_QUESTIONS, values, MySQLiteHelper.COLUMN_QUESTION_ID + "=" + id + " AND " + MySQLiteHelper.COLUMN_QUESTION_DRAWER + "<" + 5, null);
        } else {
            values.put(MySQLiteHelper.COLUMN_QUESTION_DRAWER, 0);
            database.update(MySQLiteHelper.TABLE_QUESTIONS, values, MySQLiteHelper.COLUMN_QUESTION_ID + "=" + id, null);
        }
        return getCardById(id);
    }

    private Card getCardById(int id) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_ID + " = " + id, null, null, null, null);
        if (cursor.moveToFirst()) {
            String type = cursor.getString(1);
            String question = cursor.getString(2);
            Boolean known = cursor.getInt(3)>0;
            int rating = cursor.getInt(4);
            String hint = cursor.getString(5);
            List<Answer> answers = getAnswers(id);
            int categoryId = cursor.getInt(6);
            int drawer = cursor.getInt(7);
            boolean marked = cursor.getInt(8)>0;
            Timestamp date = Timestamp.valueOf(cursor.getString(9));
            Timestamp viewed = null;
            if (!cursor.isNull(10)) {
                viewed = Timestamp.valueOf(cursor.getString(10));
            }
            Card card = new Card(id, Card.CardType.valueOf(type), question, answers, known, rating, hint, categoryId, drawer, marked, date, viewed);
            cursor.close();
            return card;
        }
        return null;
    }

    public int createMark(int id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MARKED_QUESTION_ID, id);
        long insertId = database.insert(MySQLiteHelper.TABLE_MARKED, null, values);
        return (int)insertId;
    }
    public void createMarks(List<Integer>ids) {
        for (int id : ids) {
            createMark(id);
        }
    }
    public void createMarks(Catalogue catalogue) {
        List<Card> cards;
        if (catalogue.getCategory() != null) {
            cards = getCards(catalogue.getCategory());
        } else {
            cards = new ArrayList<Card>();
            cards.add(catalogue.getCard());
        }
        for ( int i = 0; i < cards.size(); i++) {
            createMark(cards.get(i).getId());
        }
    }
    public void deleteMark(int id) {
        database.delete(MySQLiteHelper.TABLE_MARKED, MySQLiteHelper.COLUMN_MARKED_QUESTION_ID + "=" + id, null);
    }

    public void deleteMark(Catalogue catalogue) {
        List<Card> cards;
        if (catalogue.getCategory() != null) {
            cards = getCards(catalogue.getCategory());
        } else {
            cards = new ArrayList<Card>();
            cards.add(catalogue.getCard());
        }
        for ( int i = 0; i < cards.size(); i++) {
            deleteMark(cards.get(i).getId());
        }
    }

    public void deleteAllMarks() {
        database.delete(MySQLiteHelper.TABLE_MARKED, null, null);
        //database.execSQL("Drop Table "+ MySQLiteHelper.TABLE_MARKED+";");
    }

    public List<Integer> getMarks() {
        List<Integer> ids = new ArrayList<Integer>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MARKED,allMarkColumns,null,null,null,null,null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int id_quest = cursor.getInt(1);
                MarkedQuestion m = new MarkedQuestion(id, id_quest);
                ids.add(id_quest);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ids;
    }

    public List<Card> getCardsFromMarked() {
        List<Integer>ids = getMarks();
        List<Card> cards = new ArrayList<Card>();
        for (int id : ids ) {
            Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_ID +"="+id, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(0);
                    String type = cursor.getString(1);
                    String question = cursor.getString(2);
                    Boolean known = cursor.getInt(3)>0;
                    int rating = cursor.getInt(4);
                    String hint = cursor.getString(5);
                    List<Answer> answers = getAnswers(id);
                    int categoryId = cursor.getInt(6);
                    int drawer = cursor.getInt(7);
                    boolean marked = cursor.getInt(8)>0;
                    Timestamp date = Timestamp.valueOf(cursor.getString(9));
                    Timestamp viewed = null;
                    if (!cursor.isNull(10)) {
                        viewed = Timestamp.valueOf(cursor.getString(10));
                    }
                    cards.add(new Card(_id, Card.CardType.valueOf(type), question, answers, known, rating, hint, id, drawer, marked, date, viewed));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cards;
    }

    public void markCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_MARKED,true);
        database.update(MySQLiteHelper.TABLE_QUESTIONS, values, MySQLiteHelper.COLUMN_QUESTION_ID + "=" + card.getId(), null);
    }
    public void markCard(int id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_MARKED,true);
        database.update(MySQLiteHelper.TABLE_QUESTIONS,values, MySQLiteHelper.COLUMN_QUESTION_ID +"=" + id, null);
    }
    public void markCategory(int id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CATEGORY_MARKED,true);
        database.update(MySQLiteHelper.TABLE_CATEGORIES,values, MySQLiteHelper.COLUMN_CATEGORY_ID +"=" + id, null);
    }
    public void unmarkCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_MARKED,false);
        database.update(MySQLiteHelper.TABLE_QUESTIONS,values, MySQLiteHelper.COLUMN_QUESTION_ID +"=" + card.getId(), null);
    }
    public void unmarkCard(int id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION_MARKED,false);
        database.update(MySQLiteHelper.TABLE_QUESTIONS,values, MySQLiteHelper.COLUMN_QUESTION_ID +"=" + id, null);
    }
    public void unmarkCategory(int id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CATEGORY_MARKED,false);
        database.update(MySQLiteHelper.TABLE_CATEGORIES, values, MySQLiteHelper.COLUMN_CATEGORY_ID + "=" + id, null);
    }
    public void markCardOrCategory(Catalogue catalogue) {
        List<Card> cards = new ArrayList<Card>();
        if (catalogue.getCategory() != null) {
            List<Integer> parents = getAllDescendentParentIdsFromCurrentLevel(catalogue.getCategory().getId());
            for (int parId : parents) {
                markCategory(parId);
                cards.addAll(getCards(parId));
            }
        } else {
            cards = new ArrayList<Card>();
            cards.add(catalogue.getCard());
        }
        for ( int i = 0; i < cards.size(); i++) {
            markCard(cards.get(i).getId());
        }
    }
    public void unmarkCardOrCategory(Catalogue catalogue) {
        List<Card> cards = new ArrayList<Card>();
        if (catalogue.getCategory() != null) {
            //unmarkCard(catalogue.getCategory().getId());
            List<Integer> parents = getAllDescendentParentIdsFromCurrentLevel(catalogue.getCategory().getId());
            for (int parId : parents) {
                unmarkCategory(parId);
                cards.addAll(getCards(parId));
            }
        } else {
            cards = new ArrayList<Card>();
            cards.add(catalogue.getCard());
        }
        for ( int i = 0; i < cards.size(); i++) {
            unmarkCard(cards.get(i).getId());
        }
    }

    public List<Card> getMarkedCards() {
        List<Card> cards = new ArrayList<Card>();
        String orderType;
        switch(getCardsOrderType()) {
            case 0:
                orderType = MySQLiteHelper.COLUMN_QUESTION_DRAWER + " ASC";
                break;
            case 1:
                orderType = MySQLiteHelper.COLUMN_QUESTION_RATING + " ASC";
                break;
            case 2:
                orderType = MySQLiteHelper.COLUMN_QUESTION_ID + " ASC";
                break;
            default:
                orderType = null;
                break;
        }
        String lastDrawerQuery = "";
        if (isViewCardsOfLastDrawer()) {
        } else {
            lastDrawerQuery = " AND " + MySQLiteHelper.COLUMN_QUESTION_DRAWER + " < " +5;
        }
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_MARKED + " = " + 1 + lastDrawerQuery, null, null, null, orderType);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                String hint = cursor.getString(5);
                List<Answer> answers = getAnswers(id);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                cards.add(new Card(id, Card.CardType.valueOf(type), question, answers, known, rating, hint, categoryId, drawer, marked, date, viewed));
                Log.d("by mark", cards.get(cards.size()-1).toString());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }

    public int[] getDrawerDistribution() {
        int[] distr = new int[]{0,0,0,0,0,0};
        for (int i = 0; i < distr.length; i++) {
            Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS,allQuestionColumns,MySQLiteHelper.COLUMN_QUESTION_DRAWER +" = " + i, null, null, null, null);
            cursor.moveToFirst();
            distr[i] = cursor.getCount();
            cursor.close();
        }
        return distr;
    }

    public int[] getDrawerDistribution(int parentId) {
        int[] distr = new int[]{0,0,0,0,0,0};
        for (int i = 0; i < distr.length; i++) {
            Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS,allQuestionColumns,MySQLiteHelper.COLUMN_QUESTION_DRAWER +" = " + i + " AND " + MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID + " >= " + parentId, null, null, null, null);
            cursor.moveToFirst();
            distr[i] = cursor.getCount();
            cursor.close();
        }
        return distr;
    }

    public int getCardsCount() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, null, null, null, null, null);
        int c = cursor.getCount();
        cursor.close();
        return c;
    }

    public int getCardsCount(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS, allQuestionColumns, MySQLiteHelper.COLUMN_QUESTION_CATEGORY_ID + " >= " + parentId, null, null, null, null);
        int c = cursor.getCount();
        cursor.close();
        return c;
    }
    public int getCategoryCount() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CATEGORIES, allCategoryColumns, null, null, null, null, null);
        int c = cursor.getCount();
        cursor.close();
        return c;
    }
    public int getCategoryCount(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CATEGORIES, allCategoryColumns, MySQLiteHelper.COLUMN_CATEGORY_PARENT + " >= " + parentId, null, null, null, null);
        int c = cursor.getCount();
        cursor.close();
        return c;
    }

    public void setShowHint(boolean bool) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_SHOW_HINT, bool);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public void setFirstSettings() {
        database.insert(MySQLiteHelper.TABLE_SETTINGS, MySQLiteHelper.COLUMN_SETTINGS_CARDS_ORDER, null);
        if (database.query(MySQLiteHelper.TABLE_SETTINGS,allSettingsColumns,null,null,null,null,null).getCount() == 0) {
            database.insert(MySQLiteHelper.TABLE_SETTINGS, MySQLiteHelper.COLUMN_SETTINGS_CARDS_ORDER, null);
        }
    }

    public boolean isShowHint() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        boolean bool = cursor.getInt(0)>0;
        cursor.close();
        return bool;
    }

    public void setShowBar(boolean bool) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_SHOW_BAR, bool);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public boolean isShowBar() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        boolean bool = cursor.getInt(1)>0;
        cursor.close();
        return bool;
    }
    public void setOrderMultipleChoiceAnswers(boolean bool) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_MULTIPLECHOICE_CHANGE_ANSWER_ORDER, bool);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public boolean isOrderMultipleChoiceAnswers() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        boolean bool = cursor.getInt(2)>0;
        cursor.close();
        return bool;
    }
    public void setViewRandomCards(boolean bool) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_VIEW_RANDOM_CARDS, bool);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public boolean isViewRandomCards() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        boolean bool = cursor.getInt(3)>0;
        cursor.close();
        return bool;
    }
    public void setViewCardsOfLastDrawer(boolean bool) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_VIEW_LAST_DRAWER_CARDS, bool);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public boolean isViewCardsOfLastDrawer() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        boolean bool = cursor.getInt(4)>0;
        cursor.close();
        return bool;
    }
    public void setNightMode(boolean bool) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_NIGHT_MODE, bool);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public boolean isNightMode() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        boolean bool = cursor.getInt(5)>0;
        cursor.close();
        return bool;
    }

    public int getTextSizeQuestions() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        int size = cursor.getInt(6);
        cursor.close();
        return size;
    }

    public void setTextSizeQuestions(int size) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_TEXTSIZE_QUESTIONS, size);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public int getTextSizeAnswers() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        int size = cursor.getInt(7);
        cursor.close();
        return size;
    }

    public void setTextSizeAnswers(int size) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_TEXTSIZE_ANSWERS, size);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public void setOrderCards(int type) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SETTINGS_CARDS_ORDER, type);
        database.update(MySQLiteHelper.TABLE_SETTINGS, values, null, null);
    }

    public int getCardsOrderType() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SETTINGS, allSettingsColumns, null, null, null, null, null);
        cursor.moveToFirst();
        int type = cursor.getInt(8);
        cursor.close();
        return type;
    }

    /**
     * Collects the Cards for the todos activity
     * @param newest
     * @param oldest
     * @param num
     * @param drawers
     * @param listDrawers
     * @return
     */
    public List<Card> getTodosCards(boolean newest, boolean oldest, String num, boolean drawers, List<Integer> listDrawers) {
        List<Card> cards = new ArrayList<Card>();

        String whereClause = "";
        String orderBy = null;
        String limit = null;

        if (drawers) {
            for (int i = 0; i < listDrawers.size(); i++) {
                whereClause += MySQLiteHelper.COLUMN_QUESTION_DRAWER + " = " + i;
                if (i < listDrawers.size()-1) {
                    whereClause += " OR ";
                }
            }
        }
        if (newest) {
            orderBy = MySQLiteHelper.COLUMN_QUESTION_ID +" DESC";
            limit = num;
        }
        if (oldest) {
            orderBy = MySQLiteHelper.COLUMN_QUESTION_ID +" ASC";
            limit = num;
        }

        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS,allQuestionColumns ,whereClause, null, null, null, orderBy, limit);
        Log.d("WHERE", whereClause + " ORDER BY " + orderBy + " LIMIT " + limit);
        if (cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                String hint = cursor.getString(5);
                List<Answer> answers = getAnswers(_id);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                cards.add(new Card(_id, Card.CardType.valueOf(type), question, answers, known, rating, hint, _id, drawer, marked, date, viewed));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }

    public void createStatistic(int countCards, int countKnown, int countNotKnown, int countViewed, int countNotViewed, int milliseconds) {
        createStatistic(-1, countCards, countKnown, countNotKnown, countViewed, countNotViewed, milliseconds);
    }
    public void createStatistic(int parentId, int countCards, int countKnown, int countNotKnown, int countViewed, int countNotViewed, int milliseconds) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_STATISTICS_PARENT_ID, parentId);
        values.put(MySQLiteHelper.COLUMN_STATISTICS_NUM_CARDS, countCards);
        values.put(MySQLiteHelper.COLUMN_STATISTICS_NUM_KNOWN, countKnown);
        values.put(MySQLiteHelper.COLUMN_STATISTICS_NUM_NOT_KNOWN, countNotKnown);
        values.put(MySQLiteHelper.COLUMN_STATISTICS_NUM_VIEWED, countViewed);
        values.put(MySQLiteHelper.COLUMN_STATISTICS_NUM_NOT_VIEWED, countNotViewed);
        values.put(MySQLiteHelper.COLUMN_STATISTICS_TIME, milliseconds);
        database.insert(MySQLiteHelper.TABLE_STATISTICS, null, values);
    }

    public int getCountCardsStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        int num = 0;
        if (cursor.moveToFirst()) {
            do {
              num += cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return num;
    }
    public int getCountKnownStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        int num = 0;
        if (cursor.moveToFirst()) {
            do {
                num += cursor.getInt(2);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return num;
    }
    public int getCountNotKnownStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        int num = 0;
        if (cursor.moveToFirst()) {
            do {
                num += cursor.getInt(3);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return num;
    }
    public int getCountViewedStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        int num = 0;
        if (cursor.moveToFirst()) {
            do {
                num += cursor.getInt(4);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return num;
    }
    public int getCountNotViewedStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        int num = 0;
        if (cursor.moveToFirst()) {
            do {
                num += cursor.getInt(5);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return num;
    }
    public int getTimeStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        int num = 0;
        if (cursor.moveToFirst()) {
            do {
                num += cursor.getInt(6);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return num;
    }
    public List<Integer> getTimeStatisticsList(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, allStatisticsColumns, null, null, null, null, null);
        List<Integer> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getInt(6));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    public int getMaxTimeStatistics(int parentId) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATISTICS, new String [] {"MAX("+MySQLiteHelper.COLUMN_STATISTICS_TIME+")"}, null, null, null, null, null);
        int max = 0;
        if (cursor.moveToFirst()) {
            max = cursor.getInt(6);
        }
        cursor.close();
        return max;
    }

    /**
     * Returns all category ids of current level and their descendants
     * @param parentId
     * @return
     */
    public List<Integer> getAllDescendentParentIdsFromCurrentLevel(int parentId) {
        List<Integer> categoryList = new ArrayList<Integer>();
        categoryList.add(parentId);
        int countI = 0;
        int oldSize = 1;
        do {
            oldSize = categoryList.size();
            for (int i = countI; i < categoryList.size(); i++) {
                Cursor cursorCat = database.query(MySQLiteHelper.TABLE_CATEGORIES, allCategoryColumns, MySQLiteHelper.COLUMN_CATEGORY_PARENT + "=" + categoryList.get(i), null, null, null, null);
                if (cursorCat.moveToFirst()) {
                    do {
                        categoryList.add(cursorCat.getInt(0)); // id of current children
                    } while (cursorCat.moveToNext());
                }
                cursorCat.close();
                countI = i;
            }
        } while(categoryList.size() - oldSize > 0);
        // Log.d("Categories List", categoryList.toString());
        return categoryList;
    }

    public List<Card> getUnviewedCards() {
        List<Card> cards = new ArrayList<Card>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_QUESTIONS,allQuestionColumns,MySQLiteHelper.COLUMN_QUESTION_VIEWED + " = " + null,null,null,null,null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String type = cursor.getString(1);
                String question = cursor.getString(2);
                Boolean known = cursor.getInt(3)>0;
                int rating = cursor.getInt(4);
                String hint = cursor.getString(5);
                int categoryId = cursor.getInt(6);
                int drawer = cursor.getInt(7);
                boolean marked = cursor.getInt(8)>0;
                Timestamp date = Timestamp.valueOf(cursor.getString(9));
                Timestamp viewed = null;
                if (!cursor.isNull(10)) {
                    viewed = Timestamp.valueOf(cursor.getString(10));
                }
                List<Answer> answers = getAnswers(id);
                Card newCard = new Card(id, Card.CardType.valueOf(type), question, answers, known, rating, hint, categoryId, drawer, marked, date, viewed);
                Log.d("unviewed Card: ", newCard.getViewed().toString());
                cards.add(newCard);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }

}
