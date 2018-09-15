package bruh.shubham.armadilloproject;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.michaelevans.colorart.library.ColorArt;
import org.w3c.dom.Text;

import bruh.shubham.armadilloproject.Models.PackageDetails;

import static bruh.shubham.armadilloproject.Models.PackageDetails.drawableToBitmap;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {
    private final List<ApplicationInfo> appsList;
    private Context context;
    private PackageManager packageManager;
    private ItemClickListener mClickListener;
    private LayoutInflater mInflater;

    public ApplicationAdapter(Context context, List<ApplicationInfo> appsList) {
        this.context = context;
        this.appsList = appsList;
        this.mInflater = LayoutInflater.from(context);
        packageManager = context.getPackageManager();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView appName;
        ImageView iconview;
        CardView cardView;
        RelativeLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            iconview = itemView.findViewById(R.id.app_icon);
            cardView = itemView.findViewById(R.id.cv_layout);
            layout = itemView.findViewById(R.id.rv_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationInfo applicationInfo = appsList.get(position);
        if (null != applicationInfo) {
            new CalcColor(holder,position).execute();
            holder.appName.setText(applicationInfo.loadLabel(packageManager));
            holder.iconview.setImageDrawable(applicationInfo.loadIcon(packageManager));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @SuppressLint("StaticFieldLeak")
    private class CalcColor extends AsyncTask<Void, Void, Integer> {
        ViewHolder viewHolder;
        int position;

        CalcColor(ViewHolder holder, int position){
            this.viewHolder = holder;
            this.position = position;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return PackageDetails.calculateAverageColor(drawableToBitmap(appsList.get(position).loadIcon(context.getPackageManager())),5);
        }
        @Override
        protected void onPostExecute(Integer result) {
            viewHolder.layout.setBackgroundColor(result);
            super.onPostExecute(result);
        }
    }
}