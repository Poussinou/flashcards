package com.quchen.flashcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class QuestionFragment extends Fragment {

    private List<QuestionItem> questionItems = new ArrayList<>();
    private List<QuestionResult> questionResults = new ArrayList<>();

    private int numberOfDesiredQuestions;
    private int questionCount = 0;
    private int wrongAnswerCount = 0;
    private int correctAnswerCount = 0;

    private List<TextView> answerTextViews = new ArrayList<>();
    private TextView timeTextView;
    private TextView progressTextView;
    private TextView listNameTextView;
    private TextView questionTextView;
    private TextView questionSideLabel;
    private TextView answerSideLabel;
    private TextView correctCountTextView;
    private TextView wrongCountTextView;

    private void assignViews() {
        answerTextViews.add((TextView) getView().findViewById(R.id.tv_answer1));
        answerTextViews.add((TextView) getView().findViewById(R.id.tv_answer2));
        answerTextViews.add((TextView) getView().findViewById(R.id.tv_answer3));
        answerTextViews.add((TextView) getView().findViewById(R.id.tv_answer4));

        timeTextView = getView().findViewById(R.id.tv_time);
        progressTextView = getView().findViewById(R.id.tv_progress);
        listNameTextView = getView().findViewById(R.id.tv_listName);
        questionTextView = getView().findViewById(R.id.tv_question);
        questionSideLabel = getView().findViewById(R.id.tv_questionSide);
        answerSideLabel = getView().findViewById(R.id.tv_guessSide);
        correctCountTextView = getView().findViewById(R.id.tv_correctAnswerCount);
        wrongCountTextView = getView().findViewById(R.id.tv_wrongAnswerCount);
    }

    private void setUpViews() {
        setQuestionItem(questionItems.get(0));
        for(TextView tv: answerTextViews) {
            tv.setOnClickListener(answerOnClick);
        }
        updateViews();
    }

    private void updateViews() {
        progressTextView.setText(String.format(Locale.US, "%d / %d", (questionCount + 1), numberOfDesiredQuestions));
        correctCountTextView.setText(String.format(Locale.US, "%d", correctAnswerCount));
        wrongCountTextView.setText(String.format(Locale.US, "%d", wrongAnswerCount));
    }

    private void animateAnswer(final TextView tv, boolean correctAnswer) {
        int colorFrom = getResources().getColor(R.color.colorPrimary);
        int colorTo = getResources().getColor(correctAnswer ? R.color.colorCorrectAnswer : R.color.colorWrongAnswer);

        final GradientDrawable dw = (GradientDrawable) tv.getBackground();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(400);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dw.setColor((int)valueAnimator.getAnimatedValue());
            }
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // Restore to normal color
                dw.setColor(getResources().getColor(R.color.colorPrimary));

                setUpNextQuestion();
            }
        });

        colorAnimation.start();
    }

    private View.OnClickListener answerOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView answerTextView = (TextView) view;
            String givenAnswer = answerTextView.getText().toString();
            QuestionItem questionItem = questionItems.get(questionCount);
            QuestionResult questionResult = new QuestionResult(questionItem.questionHeader, questionItem.answerHeader, questionItem.question, questionItem.rightAnswer, givenAnswer);
            questionResults.add(questionResult);

            if(questionResult.isAnswerCorrect()) {
                correctAnswerCount++;
            } else {
                wrongAnswerCount++;
            }

            // Disable click event for all answers for the time of the answer animation
            for (TextView tv : answerTextViews) {
                tv.setClickable(false);
            }

            animateAnswer(answerTextView, questionResult.isAnswerCorrect());
        }
    };

    private void setQuestionItem(QuestionItem questionItem) {
        listNameTextView.setText(questionItem.listFilePath);
        questionTextView.setText(questionItem.question);
        questionSideLabel.setText(questionItem.questionHeader);
        answerSideLabel.setText(questionItem.answerHeader);

        List<String> answers = new ArrayList<>();
        answers.add(questionItem.rightAnswer);

        List<String> wrongAnswers = new ArrayList<>(questionItem.wrongAnswers);
        for(int i=0; i < (GameActivity.NUMBER_OF_ANSWERS - 1); i++) {
            String wrongAnswer = wrongAnswers.get((int) (Math.random() * wrongAnswers.size()));
            wrongAnswers.remove(wrongAnswer);
            answers.add(wrongAnswer);
        }

        Collections.shuffle(answers);

        for(int i=0; i < GameActivity.NUMBER_OF_ANSWERS; i++) {
            TextView tv = answerTextViews.get(i);
            tv.setText(answers.get(i));
            tv.setClickable(true);
        }
    }

    private void setUpNextQuestion() {
        questionCount++;

        if(questionCount == numberOfDesiredQuestions) {
            ((GameActivity)this.getActivity()).questionsCompleted(questionResults);
        } else {
            setQuestionItem(questionItems.get(questionCount));
            updateViews();
        }
    }

    public QuestionFragment() {
        // Required empty public constructor
    }

    public static QuestionFragment newInstance(int numberOfDesiredQuestions, List<QuestionItem> questionItems) {
        QuestionFragment fragment = new QuestionFragment();
        fragment.numberOfDesiredQuestions = numberOfDesiredQuestions;
        fragment.questionItems = questionItems;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assignViews();
        setUpViews();
    }
}
