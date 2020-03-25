package com.jbeach.stackquestions.ui.main;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.jbeach.stackquestions.R;

//TODO tutorial
//TODO handle wrong answer, then it gets voted correct answer
//TODO handle duplicate answers to questions (check db/tell user they already answered)
//TODO show tags for questions
//TODO paging for questions list (maybe answers if > ~10?)
//TODO character encoding
//TODO databinding, viewmodel, livedata
//TODO write instructions
//TODO implement scoring system
//TODO extract strings for multiple languages

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

    }
}