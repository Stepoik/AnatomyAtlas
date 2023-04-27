package ru.myitschool.anatomyatlas.ui.quiz_start.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ru.myitschool.anatomyatlas.R;
import ru.myitschool.anatomyatlas.data.models.BodyPart;
import ru.myitschool.anatomyatlas.data.models.Money;
import ru.myitschool.anatomyatlas.databinding.FragmentQuizStartBinding;
import ru.myitschool.anatomyatlas.ui.UseSkeleton;
import ru.myitschool.anatomyatlas.ui.custom_views.StrangeView;
import ru.myitschool.anatomyatlas.ui.quiz_start.viewModel.QuizStartViewModel;
import ru.myitschool.anatomyatlas.ui.quiz_start.viewModel.QuizStartViewModelFactory;


public class QuizStartFragment extends Fragment implements UseSkeleton {
    private FragmentQuizStartBinding binding;
    private QuizStartViewModel viewModel;
    private Map<String, StrangeView> views = new HashMap<>();
    private StrangeView selectedView;
    private final int GREEN = Color.parseColor("#8209FF00");
    private Toast noMoneyToast;
    private NavController controller;
    private int bodyCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        controller = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(this, new QuizStartViewModelFactory(getContext())).get(QuizStartViewModel.class);
        binding = FragmentQuizStartBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
    void clearFilters(){
        for (String name: views.keySet()){
            if (views.get(name) != null) {
                views.get(name).clearColorFilter();
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noMoneyToast = Toast.makeText(getContext(), "Не хватает денег, требуется 10 монет", Toast.LENGTH_SHORT);
        viewModel.getBodyParts().observe(getViewLifecycleOwner(), bodyParts -> {
            clearFilters();
            bodyCount = bodyParts.size();
            for (BodyPart bodyPart: bodyParts){
                StrangeView v = views.get(bodyPart.getName());
                if (v != null) {
                    v.setColorFilter(GREEN);
                }
            }
        });
        viewModel.getMoneyContainer().observe(getViewLifecycleOwner(), money -> {
            if (money == null){
                viewModel.createMoney();
                return;
            }
            binding.money.setText(String.valueOf(money.getValue()));
        });
        binding.buy.setOnClickListener((v)->{
            if (selectedView != null){
                if (viewModel.buyBodyPart(selectedView.getName())){
                    selectedView.stopAnimation();
                    selectedView = null;
                    binding.buy.setVisibility(View.GONE);
                    binding.name.setText("");
                    return;
                }
                noMoneyToast.show();
            }
        });
        binding.startButton.setOnClickListener(v -> {
            if (bodyCount >= 5) {
                controller.navigate(R.id.action_quizStartFragment_to_quizFragment);
                return;
            }
            Toast.makeText(getContext(), "У вас должно быть хотя бы 5 купленных частей тела", Toast.LENGTH_SHORT).show();
        });
    }
    void fillViewList(List<ViewGroup> listViews){
        for (ViewGroup viewGroup: listViews){
            for (int i = 0; i < viewGroup.getChildCount(); i++){
                StrangeView currentView = (StrangeView)viewGroup.getChildAt(i);
                views.put(currentView.getName(), currentView);
                currentView.setOnClickListener(v -> {
                    if (currentView != selectedView){
                        if (selectedView != null){
                            selectedView.stopAnimation();
                        }
                        binding.name.setText(currentView.getName());
                        selectedView = currentView;
                        if (!viewModel.isAlreadyBought(currentView.getName())) {
                            binding.buy.setVisibility(View.VISIBLE);
                            selectedView.startAnimation();
                        }
                        else{
                            binding.buy.setVisibility(View.GONE);
                        }
                    }
                    else{
                        selectedView.stopAnimation();
                        binding.buy.setVisibility(View.GONE);
                        binding.name.setText("");
                        selectedView = null;
                    }
                });
            }
        }
    }

    @Override
    public void onLoadChildFragment(List<ViewGroup> views) {
        fillViewList(views);
    }
}