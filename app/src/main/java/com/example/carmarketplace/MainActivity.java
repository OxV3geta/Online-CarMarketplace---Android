
package com.example.carmarketplace;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class MainActivity extends AppCompatActivity {
    private FrameLayout contentFrame;
    private FrameLayout drawerLayer;
    private TextView toolbarTitle;
    private LinearLayout bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        toolbarTitle = findViewById(R.id.toolbarTitle);
        findViewById(R.id.menuButton).setOnClickListener(v -> openDrawer());
        findViewById(R.id.profileToolbarButton).setOnClickListener(v -> showProfile());

        // Content
        contentFrame = findViewById(R.id.contentFrame);

        // Bottom Nav
        bottomNav = findViewById(R.id.bottomNav);
        findViewById(R.id.navHome).setOnClickListener(v -> showDashboard());
        findViewById(R.id.navSearch).setOnClickListener(v -> showSearch(""));
        findViewById(R.id.navSell).setOnClickListener(v -> showSell());
        findViewById(R.id.navMessages).setOnClickListener(v -> showMessages());
        findViewById(R.id.navProfile).setOnClickListener(v -> showProfile());

        // Drawer
        drawerLayer = findViewById(R.id.drawerLayer);
        findViewById(R.id.drawerShade).setOnClickListener(v -> closeDrawer());
        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            new SessionManager(this).logout();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        showDashboard();
    }

    private void openDrawer() {
        drawerLayer.setVisibility(View.VISIBLE);
        LinearLayout itemsList = findViewById(R.id.drawerItemsList);
        itemsList.removeAllViews();

        SessionManager session = new SessionManager(this);

        // Header Section
        View header = getLayoutInflater().inflate(R.layout.drawer_header, itemsList, false);
        ImageView avatar = header.findViewById(R.id.userAvatar);
        TextView nameText = header.findViewById(R.id.userName);
        View viewProfileBtn = header.findViewById(R.id.viewProfileBtn);

        nameText.setText(session.getName().isEmpty() ? "User" : session.getName());
        String img = session.getProfileImageUrl();
        if (!img.isEmpty()) {
            Glide.with(this).load(img).transform(new CircleCrop()).into(avatar);
        }
        viewProfileBtn.setOnClickListener(v -> { closeDrawer(); showProfile(); });
        itemsList.addView(header);

        // Menu Items
        addSidebarItem(itemsList, "⊞", "Seller Dashboard", true, v -> { closeDrawer(); showDashboard(); });
        addSidebarItem(itemsList, "🗺", "Maps / Near Me", false, v -> {
            closeDrawer();
            startActivity(new Intent(this, MapActivity.class));
        });

        // Section Label
        TextView aiToolsLabel = UiUtils.text(this, "AI TOOLS", 11, getColor(R.color.text_secondary), true);
        aiToolsLabel.setPadding(UiUtils.dp(this, 24), UiUtils.dp(this, 20), 0, UiUtils.dp(this, 12));
        itemsList.addView(aiToolsLabel);

        addSidebarItem(itemsList, "📊", "Cost Analyzer", false, v -> {
            closeDrawer();
            startActivity(new Intent(this, CostAnalyzerActivity.class));
        });
        addSidebarItem(itemsList, "💬", "AI Chatbot Assistant", false, v -> Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show());

        // Divider
        View line = new View(this);
        line.setBackgroundColor(getColor(R.color.border));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
        lp.setMargins(0, UiUtils.dp(this, 12), 0, UiUtils.dp(this, 12));
        itemsList.addView(line, lp);

        addSidebarItem(itemsList, "⚙", "Settings", false, v -> {
            closeDrawer();
            startActivity(new Intent(this, SettingsActivity.class));
        });
        addSidebarItem(itemsList, "ⓘ", "Help & Support", false, v -> Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show());
    }

    private void addSidebarItem(LinearLayout container, String icon, String label, boolean active, View.OnClickListener click) {
        View view = getLayoutInflater().inflate(R.layout.item_sidebar, container, false);
        TextView iconTv = view.findViewById(R.id.itemIcon);
        TextView labelTv = view.findViewById(R.id.itemLabel);

        iconTv.setText(icon);
        labelTv.setText(label);

        if (active) {
            view.setBackground(UiUtils.rounded(getColor(R.color.dark_card), UiUtils.dp(this, 12)));
            iconTv.setTextColor(getColor(R.color.lime));
            labelTv.setTextColor(getColor(R.color.text_primary));
        } else {
            iconTv.setTextColor(getColor(R.color.text_secondary));
            labelTv.setTextColor(getColor(R.color.text_secondary));
        }

        view.setOnClickListener(click);
        UiUtils.pressEffect(view);
        container.addView(view);
    }

    private void closeDrawer() {
        drawerLayer.setVisibility(View.GONE);
    }

    public void showDashboard() {
        toolbarTitle.setText("Home");
        updateNavSelection(R.id.navHome);
        replace(new DashboardFragment());
    }

    public void showSearch(String query) {
        toolbarTitle.setText("Search");
        updateNavSelection(R.id.navSearch);
        SearchFragment f = new SearchFragment();
        Bundle b = new Bundle();
        b.putString("query", query);
        f.setArguments(b);
        replace(f);
    }

    public void showSell() {
        toolbarTitle.setText("Sell");
        updateNavSelection(R.id.navSell);
        replace(new SellFragment());
    }

    public void showMessages() {
        toolbarTitle.setText("Messages");
        updateNavSelection(R.id.navMessages);
        replace(new MessagesFragment());
    }

    public void showProfile() {
        toolbarTitle.setText("Profile");
        updateNavSelection(R.id.navProfile);
        replace(new ProfileFragment());
    }

    private void updateNavSelection(int selectedId) {
        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            View v = bottomNav.getChildAt(i);
            if (v instanceof LinearLayout) {
                LinearLayout item = (LinearLayout) v;
                TextView icon = (TextView) item.getChildAt(0);
                TextView label = (TextView) item.getChildAt(1);
                if (item.getId() == selectedId) {
                    icon.setTextColor(getColor(R.color.lime));
                    label.setTextColor(Color.WHITE);
                } else {
                    icon.setTextColor(getColor(R.color.text_secondary));
                    label.setTextColor(getColor(R.color.text_secondary));
                }
            }
        }
    }

    private void replace(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(contentFrame.getId(), f).commit();
    }
}
