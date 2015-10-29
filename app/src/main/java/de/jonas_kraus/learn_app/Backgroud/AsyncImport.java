package de.jonas_kraus.learn_app.Backgroud;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.activity.ImportActivity;

/**
 * Created by Jonas on 29.10.2015.
 */
public class AsyncImport extends AsyncTask<Void, Integer, Boolean> {

    private ImportActivity importActivity;
    private List<Catalogue> catalogue;
    private DbManager db;
    private int currentCategoryParent = -1;
    private ProgressDialog mProgressDialog;

    public AsyncImport(ImportActivity importActivity, List<Catalogue> catalogue, DbManager db, int currentCategoryParent) {
        this.importActivity = importActivity;
        this.catalogue = catalogue;
        this.db = db;
        this.currentCategoryParent = currentCategoryParent;
        mProgressDialog = new ProgressDialog(importActivity);
        mProgressDialog.setMessage("Importing Cards");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        publishProgress(25);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        db.importCatalogue(catalogue,currentCategoryParent);
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setMessage("Import start");
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        super.onProgressUpdate();
        mProgressDialog.setProgress(progress[0]);
        Log.d("import progress", "update "+mProgressDialog.getProgress());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mProgressDialog.dismiss();
        Log.d("import progress","post");
    }
}
