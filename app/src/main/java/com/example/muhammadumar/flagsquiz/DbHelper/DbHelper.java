package com.example.muhammadumar.flagsquiz.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.muhammadumar.flagsquiz.Common.Common;
import com.example.muhammadumar.flagsquiz.Model.Question;
import com.example.muhammadumar.flagsquiz.Model.Ranking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Umar on 5/15/2018.
 */

public class DbHelper extends SQLiteOpenHelper{



    private static String DB_NAME = "mydb.db";
    private static String DB_PATH = "";
    private SQLiteDatabase mDataBase;
    private Context mContext = null;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, 1);

        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        openDataBase(); // Add this line to fix db.insert can't insert values
        this.mContext = context;
    }

    public void openDataBase(){
        String myPath = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READWRITE);
    }

    public void copyDataBase() throws IOException{
        try{
            InputStream myInput = mContext.getAssets().open(DB_NAME);
            String outputFileName = DB_PATH+DB_NAME;
            OutputStream myOutput = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;
            while((length = myInput.read(buffer))> 0)
            {
                myOutput.write(buffer,0,length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();;
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean checkDataBase(){
        SQLiteDatabase tempDB = null;
        try{
            String myPath = DB_PATH+DB_NAME;
            tempDB = SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READWRITE);

        }
        catch (SQLiteException e){
            e.printStackTrace();
        }
        if(tempDB != null)
            tempDB.close();
        return tempDB!=null?true:false;
    }

    public void createDataBase() throws IOException{
        boolean isDBExists = checkDataBase();
        if (isDBExists) {

        }
        else
        {
            this.getReadableDatabase();
            try{
                copyDataBase();;
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }

    }


    @Override
    public synchronized void close() {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public List<Question> getAllQuestion() {

        List<Question> listQuestion = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        try {
            c = db.rawQuery("SELECT * FROM Question ORDER BY Randon()", null);
            if (c == null) return null;
            c.moveToFirst();
            do{
                int Id = c.getInt(c.getColumnIndex("ID"));
                String Image = c.getString(c.getColumnIndex("Image"));
                String AnswerA = c.getString(c.getColumnIndex("AnswerA"));
                String AnswerB = c.getString(c.getColumnIndex("AnswerB"));
                String AnswerC = c.getString(c.getColumnIndex("AnswerC"));
                String AnswerD = c.getString(c.getColumnIndex("AnswerD"));
                String CorrectAnswer = c.getString(c.getColumnIndex("CorrectAnswer"));

                Question question = new Question(Id,Image,AnswerA,AnswerB,AnswerC,AnswerD,CorrectAnswer);
                listQuestion.add(question);
            }
            while(c.moveToNext());
            c.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return listQuestion;

    }

    public List<Question> getQuestionMode(String mode) {
        List<Question> listQuestion = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        int limit = 0;
        if (mode.equals(Common.MODE.EASY.toString()))
            limit = 30;
        else if (mode.equals(Common.MODE.MEDIUM.toString()))
            limit = 50;
        else if (mode.equals(Common.MODE.HARD.toString()))
            limit = 100;
        else if (mode.equals(Common.MODE.HARDEST.toString()))
            limit = 200;
        try {
            c = db.rawQuery(String.format("SELECT * FROM Question ORDER BY Random() LIMIT %d", limit), null);
            if (c == null) return null;
            c.moveToFirst();
            do {
                int Id = c.getInt(c.getColumnIndex("ID"));
                String Image = c.getString(c.getColumnIndex("Image"));
                String AnswerA = c.getString(c.getColumnIndex("AnswerA"));
                String AnswerB = c.getString(c.getColumnIndex("AnswerB"));
                String AnswerC = c.getString(c.getColumnIndex("AnswerC"));
                String AnswerD = c.getString(c.getColumnIndex("AnswerD"));
                String CorrectAnswer = c.getString(c.getColumnIndex("CorrectAnswer"));

                Question question = new Question(Id, Image, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer);
                listQuestion.add(question);
            }
            while (c.moveToNext());
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return listQuestion;
    }

    public void insertScore(int Score){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Score", Score);
        db.insert("Ranking",null,contentValues);
    }

    public List<Ranking> getRanking(){
        List<Ranking> listRanking = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        try{
            c = db.rawQuery("SELECT * FROM  Ranking ORDER BY Score DESC", null );
            if(c==null) return null;
            c.moveToNext();
            do{
                int Id = c.getInt(c.getColumnIndex("ID"));
                int Score = c.getInt(c.getColumnIndex("Score"));

                Ranking ranking = new Ranking(Id,Score);
                listRanking.add(ranking);
            }
            while(c.moveToNext());
            c.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        db.close();
        return listRanking;

    }

    public int getPlayCount(int level)
    {
        int result = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c;
        try{
            c = db.rawQuery("SELECT PlayCount FROM UserPlayCount WHERE Level="+level+";",null);
            if(c == null) return 0;
            c.moveToNext();
            do{
                result  = c.getInt(c.getColumnIndex("PlayCount"));
            }while(c.moveToNext());
            c.close();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    public void updatePlayCount(int level,int playCount)
    {
        String query = String.format("UPDATE UserPlayCount Set PlayCount = %d WHERE Level = %d",playCount,level);
        mDataBase.execSQL(query);
    }


}
