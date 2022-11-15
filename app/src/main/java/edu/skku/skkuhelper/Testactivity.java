package edu.skku.skkuhelper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.lifecycle.Observer;

import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.ToDoAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.UserCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import edu.skku.skkuhelper.roomdb.SKKUNotice;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDB;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDao;

import edu.skku.skkuhelper.roomdb.UserInfoDB;
import edu.skku.skkuhelper.roomdb.UserInfoDao;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class Testactivity{
	public static void main(String[] args){
		
		ArrayList<SKKUNotice> SKKUnotice = new ArrayList<>();
		
		SKKUNoticeDB SKKUnoticeDB = null;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);

			
			SKKUnoticeDB = SKKUNoticeDB.getInstance(this);
			
			SKKUnotice = SKKUnoticeDB.SKKUnoticeDao().getAll();
			
			ArrayList<SKKUNotice> listts = new ArrayList<SKKUNotice>();
			
			listts.add(new SKKUNotice("title2", "sum2", 2, 345, "kim", "2023-1-12", "testlink2"));
			
			@override
			new Inserttest(SKKUnoticeDB.SKKUnoticeDao()).execute(listts);
			

		}
	}
	
	
	
	public static class Inserttest extends AsyncTask<SKKUNotice, Void, Void>{
		private final SKKUNoticeDao mSKKUnoticeDao;
		
		public Inserttest(SKKUNoticeDao SKKUnoticeDao){
		  this.mSKKUnoticeDao = SKKUnoticeDao;
		}
		
		@Override
		protected Void doInBackground(SKKUNotice... SKKUnotices) {
		  mSKKUnoticeDao.insert(SKKUNotices[0]);
		  return null;
		}
        //
		// 메인 스레드에서 DB 접근할 수 없으니 AsyncTask를 사용
  
    }
}