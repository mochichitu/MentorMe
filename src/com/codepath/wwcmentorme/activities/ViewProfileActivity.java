package com.codepath.wwcmentorme.activities;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.TextView;

import com.codepath.wwcmentorme.R;
import com.codepath.wwcmentorme.app.MentorMeApp;
import com.codepath.wwcmentorme.data.DataService;
import com.codepath.wwcmentorme.helpers.Utils;
import com.codepath.wwcmentorme.models.Rating;
import com.codepath.wwcmentorme.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

public class ViewProfileActivity extends AppActivity {
	public static final String USER_ID_KEY = "userId";
	public static final String LATITUDE_KEY = "latitude";
	public static final String LONGITUDE_KEY = "longitude";
	private User user;
	private ImageLoader mImageLoader;
	private ImageView ivMentorProfile;
	private Double mLat;
	private Double mLng;
	private TextView tvFirstName;
	private TextView tvLastName;
	private TextView tvPosition;
	private TextView tvLocation;
	private TextView tvDistance;
	private TextView tvMenteeCount;
	private RatingBar rbRating;
	private TextView tvNoOfRating;
	private TextView tvAddReview;
	private TextView tvAboutMe;
	private LinearLayout llMentorSkills;
	private LinearLayout llMenteeSkills;
	private TextView tvYearsExperience;
	private TextView tvMentorSkills;
	private TextView tvMenteeSkills;
	private TextView tvAvailabilityHeader;
	private LinearLayout llAvailability;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_profile);
		
		if(getIntent().hasExtra(LATITUDE_KEY)) {
			mLat = getIntent().getDoubleExtra(LATITUDE_KEY, 0);
		}
		
		if(getIntent().hasExtra(LONGITUDE_KEY)) {
			mLng = getIntent().getDoubleExtra(LONGITUDE_KEY, 0);
		}
		
		if(getIntent().hasExtra(USER_ID_KEY)) {
			final int userId = getIntent().getIntExtra(USER_ID_KEY, 0);
			DataService.getUser(userId, new FindCallback<User>() {
				
				@Override
				public void done(List<User> theUsers, ParseException e) {
					if (e == null) {
						if(theUsers != null) {
							for (User theUser : theUsers) {											
								user = theUser;
								setupViews();
								populateViews();
								break;
							}
						}
					} else {
						e.printStackTrace();
					}
				}
			});				
		}		
	}

	private void setupViews() {
		ivMentorProfile = (ImageView) findViewById(R.id.ivMentorProfile);
		tvFirstName = (TextView) findViewById(R.id.tvFirstName);
		tvLastName = (TextView) findViewById(R.id.tvLastName);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvLocation = (TextView) findViewById(R.id.tvLocation);
		tvDistance = (TextView) findViewById(R.id.tvDistance);
		tvMenteeCount = (TextView) findViewById(R.id.tvMenteeCount);
		rbRating = (RatingBar) findViewById(R.id.rbRating);
		LayerDrawable stars;
		stars = (LayerDrawable) rbRating.getProgressDrawable();
		stars.getDrawable(2).setColorFilter(Color.parseColor("#00B6AA"), PorterDuff.Mode.SRC_ATOP);
		tvNoOfRating = (TextView) findViewById(R.id.tvNoOfRating);
		tvAddReview = (TextView) findViewById(R.id.tvAddReview);
		tvAboutMe = (TextView) findViewById(R.id.tvAboutMe);
		llMentorSkills = (LinearLayout) findViewById(R.id.llMentorSkills);
		llMenteeSkills = (LinearLayout) findViewById(R.id.llMenteeSkills);
		tvYearsExperience = (TextView) findViewById(R.id.tvYearsExperience);
		tvMentorSkills = (TextView) findViewById(R.id.tvMentorSkills);
		tvMenteeSkills = (TextView) findViewById(R.id.tvMenteeSkills);
		tvAvailabilityHeader = (TextView) findViewById(R.id.tvAvailabilityHeader);
		llAvailability = (LinearLayout) findViewById(R.id.llAvailability);
	}

	private void populateViews() {
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(this));
		mImageLoader.displayImage(user.getProfileImageUrl(640), ivMentorProfile);
		
		tvFirstName.setText(user.getFirstName());
		tvLastName.setText(user.getLastName());
		
		String formattedPosition = user.getJobTitle()  + ", " + user.getCompanyName();
		tvPosition.setText(Html.fromHtml(formattedPosition));
		
		String formattedLocation = user.getCity()  + ", " + user.getZip();
		tvLocation.setText(Html.fromHtml(formattedLocation));
		
		Double distance = Utils.getDistance(mLat, mLng, user.getLocation().getLatitude(), user.getLocation().getLongitude());
		tvDistance.setText(Utils.formatDouble(distance) + "mi");	
		tvAboutMe.setText(user.getAboutMe());

		populateMenteeCount();
		populateAverageRating();
		populateAddReview();	
		populateMentorSkills();
		populateMenteeSkills();
		populateAvailability();
		
		tvYearsExperience.setText(Integer.toString(user.getYearsExperience()));
	}
	
	private void populateMenteeCount() {
		DataService.getMenteeCount(user.getFacebookId(), new CountCallback() {
			
			@Override
			public void done(int count, ParseException arg1) {
				if(count > 0) {
					tvMenteeCount.setText(Integer.toString(count) + " " + getResources().getQuantityString(R.plurals.mentee, count));
				}
			}
		});		
	}

	private void populateAvailability() {
		if(user.getAvailability() != null && user.getAvailability().length() > 0) {
			 for (String day : getResources().getStringArray(R.array.dayofweek_array)) {
				 TextView tvAvailabilityDay = (TextView) getLayoutInflater().inflate(R.layout.availability_textview_item, null);
				 tvAvailabilityDay.setText(day);
				 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(0,0,15,0);
				tvAvailabilityDay.setLayoutParams(params);
				 if(user.getAvailability().toString().contains(day)) {
					 tvAvailabilityDay.setBackgroundColor(getResources().getColor(R.color.actionbar));
					 tvAvailabilityDay.setTextColor(Color.parseColor("#ffffff"));
				 } 
				 llAvailability.addView(tvAvailabilityDay);
			}
			
		} else {
			tvAvailabilityHeader.setVisibility(View.GONE);
			llAvailability.setVisibility(View.GONE);
		}
	}

	private void populateMenteeSkills() {
		if(user.getMenteeSkills() != null && user.getMenteeSkills().length() > 0) {
			for(int i = 0; i <= user.getMenteeSkills().length() - 1; i++) {
				TextView tvMenteeSkill = (TextView) getLayoutInflater().inflate(R.layout.skill_textview_item, null);
				try {
					tvMenteeSkill.setText(user.getMenteeSkills().get(i).toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(0,0,10,0);
				tvMenteeSkill.setLayoutParams(params);
				
			    llMenteeSkills.addView(tvMenteeSkill); 
			}
		} else {
			tvMenteeSkills.setVisibility(View.GONE);
		}
	}

	private void populateMentorSkills() {
		if(user.getMentorSkills() != null && user.getMentorSkills().length() > 0) {
			for(int i = 0; i <= user.getMentorSkills().length() - 1; i++) {
				TextView tvMentorSkill = (TextView) getLayoutInflater().inflate(R.layout.skill_textview_item, null);
				try {
					tvMentorSkill.setText(user.getMentorSkills().get(i).toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(0,0,10,0);
				tvMentorSkill.setLayoutParams(params);
				
			    llMentorSkills.addView(tvMentorSkill);
			}
		} else {
			tvMentorSkills.setVisibility(View.GONE);
		}
	}

	private void populateAddReview() {
		DataService.getRatingByUser(MentorMeApp.getCurrentUser().getFacebookId(), user.getFacebookId(), new GetCallback<Rating>() {
			
			@Override
			public void done(Rating rating, ParseException e) {
				if(e == null) {
					if(rating != null) {
						float numRating = 0;
						numRating += rating.getRating();
						tvAddReview.setText("Edit review");
					} 
				} else {
					tvAddReview.setText("Add review");
					e.printStackTrace();
				}				
			}
		});
	}
	
	private void populateAverageRating() {
		DataService.getAverageRating(user.getFacebookId(), new FindCallback<Rating>() {
			
			@Override
			public void done(List<Rating> ratings, ParseException e) {
				if(e == null) {
					if(ratings.size() > 0) {
						float numRating = 0;
						int count = 0;
						for (Rating rating : ratings) {
							if(rating.getRating() > 0) {
								count++;
								numRating += rating.getRating();
							}
						}
						
						numRating = numRating/count;
						tvNoOfRating.setText(Integer.toString(count));
						rbRating.setRating(numRating);
						
					} else {
						rbRating.setRating(0);
						tvNoOfRating.setText("0");
					}
				} else {
					e.printStackTrace();
				}
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view_profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
