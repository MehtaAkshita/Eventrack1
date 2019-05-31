package com.example.eventrack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list=blog_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context=parent.getContext();

        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId=firebaseAuth.getCurrentUser().getUid();

        String desc_data=blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String venue_data=blog_list.get(position).getVenue();
        holder.setVenueText(venue_data);

        String date_data=blog_list.get(position).getDate();
        holder.setDateText(date_data);

        String time_data=blog_list.get(position).getTime();
        holder.setTimeText(time_data);

        String image_url=blog_list.get(position).getImage_url();
        String thumbUri = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url, thumbUri);

        String user_id=blog_list.get(position).getUser_id();
        //User data retrieval

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName, userImage);


                } else {

//                    String error = task.getException().getMessage();
//                    Toast.makeText(BlogRecyclerAdapter.this, Toast.LENGTH_LONG, String.format("Firebase Exception", error)).show();

                }

            }
        });

        long millisecond=blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
        holder.setTime(dateString);


        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){
                    int count = documentSnapshots.size();
                    holder.updateLikesCount(count);

                } else {
                    holder.updateLikesCount(0);
                }
            }
        });


        //Get Likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                } else {

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                }
            }
        });


        //Likes Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String,Object> likesMap=new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                        }else{
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        private TextView descView;
        private TextView venueView;
        private TextView dateView;
        private TextView timeView;
        private ImageView blogImageView;
        private TextView blogDate;

        private TextView blogUserName;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setDescText(String descText){
            descView=mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setVenueText(String venueText){
            venueView=mView.findViewById(R.id.blog_venue);
            venueView.setText(venueText);

        }

        public void setDateText(String dateText){
            dateView=mView.findViewById(R.id.blog_event_date);
            dateView.setText(dateText);

        }

        public void setTimeText(String timeText){
            timeView=mView.findViewById(R.id.blog_event_time);
            timeView.setText(timeText);

        }

        public void setBlogImage( String thumbUri, String downloadUri){
            blogImageView=mView.findViewById(R.id.blog_image);

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.new_post_default_post_pic);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);        }

        public void setTime(String date){

            blogDate=mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name,String image){

            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.new_post_default_profile_pic);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }

        public void updateLikesCount(int count){
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");
        }
    }

}
