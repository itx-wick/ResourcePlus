package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.utils.Utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private Context context;
    private List<Media> mediaList;

    public DocumentAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentAdapter.ViewHolder holder, int position) {
        Media media = mediaList.get(position);

        String fileName = media.getPath().split(";")[1];
        String fullPath = Utils.getDirectoryPath(fileName) + fileName;
        File file = new File(fullPath);

        holder.tvName.setText(fileName);
        holder.date.setText(getTime(media.getPath().split(";")[2]));
        double size = Double.parseDouble(media.getPath().split(";")[3]);
        size = size / 1024.0;
        holder.size.setText(String.format("%.2f", size) + " MB");

//        if (fullPath.contains(".pdf"))
//            holder.document.setImageBitmap(pdfToBitmap(file));
//        else
//            holder.document.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_document));

        holder.root.setOnClickListener(v -> {
            if (Utils.isFilePresent(media.getPath().split(";")[0])) {
                Utils.openFile(file, context);
            }
        });

    }

    public String getTime(String createdAt) {
        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(createdAt);
            SimpleDateFormat pstFormat = new SimpleDateFormat("h:mm aa - dd/MM/yyyy");
            pstFormat.setTimeZone(TimeZone.getDefault());
            return pstFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap pdfToBitmap(File pdfFile) {
        Bitmap bitmap = null;
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            final int pageCount = renderer.getPageCount();
            if (pageCount > 0) {
                PdfRenderer.Page page = renderer.openPage(0);
                int width = page.getWidth();
                int height = page.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        ImageView document;
        TextView tvName, size, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            document = itemView.findViewById(R.id.document);
            tvName = itemView.findViewById(R.id.tvName);
            size = itemView.findViewById(R.id.size);
            date = itemView.findViewById(R.id.date);
        }
    }

}
