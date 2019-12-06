package com.example.ARmirrorS.Server.Utils;

import android.app.Activity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.R;

import org.opencv.video.Video;

public class ServerGUISetup {

    private Activity parentActivity;


    /**
     *
     * @param parent
     */
    public ServerGUISetup(Activity parent) {
        parentActivity = parent;
    }

    /**
     *
     * @param position
     * @param frame
     */
    public void run(final int position, Frame frame) {

        // Setup Chunk View
        setupChunk(position);

        // Setup Radio Button for subtraction Mode.
        setupRadioButtons(position, frame);

        // Setup SeekBar Listeners callback functions to monitor user input-progress
        setupSeekBars(position, frame);
    }

    /**
     *
     * @param position
     */
    private void setupChunk(final int position) {

        LinearLayout parentView = parentActivity.findViewById(R.id.test);
        parentView.removeAllViews();
        parentView.setVisibility(View.VISIBLE);

        switch (position) {

            case ImageProcessParam.BG_SUBTRACT_ADAPTIVE: {
                View chunk = parentActivity.getLayoutInflater()
                        .inflate(
                                R.layout.chunk_adaptive_settings,
                                parentView,
                                false
                        );
                parentView.addView(chunk);
            } break;

            case ImageProcessParam.BG_SUBTRACT_BINARY: {
                View chunk = parentActivity.getLayoutInflater()
                        .inflate(
                                R.layout.chunk_binary_settings,
                                parentView,
                                false
                        );
                parentView.addView(chunk);
            } break;

            case ImageProcessParam.BG_SUBTRACT_DIFF: {
                View chunk = parentActivity.getLayoutInflater()
                        .inflate(
                                R.layout.chunk_subtraction_settings,
                                parentView,
                                false
                        );
                parentView.addView(chunk);
            } break;

            case ImageProcessParam.BG_SUBTRACT_HSV: {
                View chunk = parentActivity.getLayoutInflater()
                        .inflate(
                                R.layout.chunk_hsv_settings,
                                parentView,
                                false
                        );
                parentView.addView(chunk);
            } break;

            case ImageProcessParam.BG_SUBTRACT_OTSU: {
                View chunk = parentActivity.getLayoutInflater()
                        .inflate(
                                R.layout.chunk_otsu_settings,
                                parentView,
                                false
                        );
                parentView.addView(chunk);
            } break;

            case ImageProcessParam.BG_SUBTRACT_CANNY: {
                View chunk = parentActivity.getLayoutInflater()
                        .inflate(
                                R.layout.chunk_canny_settings,
                                parentView,
                                false
                        );
                parentView.addView(chunk);
            } break;

            default:
                break;
        }
    }

    /**
     *
     * @param position
     * @param frame
     */
    private void setupRadioButtons(final int position, Frame frame) {

        switch (position) {

            case ImageProcessParam.BG_SUBTRACT_ADAPTIVE: {
                RadioGroup modeGroup = parentActivity.findViewById(R.id.radioGroupAdaptive);
                modeGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    switch (checkedId) {
                        case R.id.adaptiveMean:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_ADAPTIVE_MEAN);
                            break;
                        case R.id.adaptiveGaussian:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_ADAPTIVE_GAUSSIAN);
                            break;
                        default:
                            break;
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_BINARY: {
                RadioGroup modeGroup = parentActivity.findViewById(R.id.radioGroupBinary);
                modeGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    switch (checkedId) {
                        case R.id.threshBinary:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_BINARY_ONLY);
                            break;
                        case R.id.threshBinaryInv:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_BINARY_INV);
                            break;
                        case R.id.threshTruncate:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_BINARY_TRUNCATE);
                            break;
                        case R.id.threshToZero:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO);
                            break;
                        case R.id.threshToZeroInv:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO_INV);
                            break;
                        default:
                            break;
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_DIFF: {
                RadioGroup modeGroup = parentActivity.findViewById(R.id.radioGroupSubtraction);
                TableRow row1  = parentActivity.findViewById(R.id.row1);
                TableRow row2  = parentActivity.findViewById(R.id.row2);
                TableRow row3  = parentActivity.findViewById(R.id.row3);
                TableRow row4  = parentActivity.findViewById(R.id.row4);
                TableRow row5  = parentActivity.findViewById(R.id.row5);
                TableRow row6  = parentActivity.findViewById(R.id.row6);
                TableRow row7  = parentActivity.findViewById(R.id.row7);
                TableRow row8  = parentActivity.findViewById(R.id.row8);
                TableRow row9  = parentActivity.findViewById(R.id.row9);
                TableRow row10 = parentActivity.findViewById(R.id.row10);
                TableRow row11 = parentActivity.findViewById(R.id.row11);
                TableRow row12 = parentActivity.findViewById(R.id.row12);

                modeGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    switch (checkedId) {
                        case R.id.absFirst:
                        case R.id.absSeq:
                            row1.setVisibility(View.GONE);
                            row2.setVisibility(View.GONE);
                            row3.setVisibility(View.GONE);
                            row4.setVisibility(View.GONE);
                            row5.setVisibility(View.GONE);
                            row6.setVisibility(View.GONE);
                            row7.setVisibility(View.GONE);
                            row8.setVisibility(View.GONE);
                            row9.setVisibility(View.VISIBLE);
                            row10.setVisibility(View.VISIBLE);
                            row11.setVisibility(View.GONE);

                            frame.setCaptured1stFrame(false);

                            if (checkedId == R.id.absFirst) {
                                frame.firstGray = null;
                                row12.setVisibility(View.VISIBLE);
                                frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST);
                                boolean timerStatus = ((MirrorApp) parentActivity
                                                        .getApplication()).getTimerStatus();

                                if (timerStatus == false) {
                                    FrameCaptureTimer.start(parentActivity, frame);
                                    ((MirrorApp) parentActivity.getApplication()).setTimerStatus(true);
                                }
                            } else {
                                frame.previousGray = null;
                                row12.setVisibility(View.GONE);
                                frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_DIFF_ABS_SEQ);
                            }
                            break;

                        case R.id.mog2:
                            row1.setVisibility(View.VISIBLE);
                            row2.setVisibility(View.VISIBLE);
                            row3.setVisibility(View.VISIBLE);
                            row4.setVisibility(View.VISIBLE);
                            row5.setVisibility(View.VISIBLE);
                            row6.setVisibility(View.VISIBLE);
                            row7.setVisibility(View.VISIBLE);
                            row8.setVisibility(View.VISIBLE);
                            row9.setVisibility(View.VISIBLE);
                            row10.setVisibility(View.VISIBLE);
                            row11.setVisibility(View.VISIBLE);
                            row12.setVisibility(View.GONE);

                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_DIFF_MOG2);
                            frame.backgroundBufferMOG2 = Video.createBackgroundSubtractorMOG2();
                            break;

                        case R.id.knn:
                            row1.setVisibility(View.GONE);
                            row2.setVisibility(View.GONE);
                            row3.setVisibility(View.VISIBLE);
                            row4.setVisibility(View.VISIBLE);
                            row5.setVisibility(View.GONE);
                            row6.setVisibility(View.GONE);
                            row7.setVisibility(View.GONE);
                            row8.setVisibility(View.GONE);
                            row9.setVisibility(View.VISIBLE);
                            row10.setVisibility(View.VISIBLE);
                            row11.setVisibility(View.VISIBLE);
                            row12.setVisibility(View.GONE);

                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_DIFF_KNN);
                            frame.backgroundBufferKNN = Video.createBackgroundSubtractorKNN();
                            break;

                        default:
                            break;
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_CANNY: {
                RadioGroup modeGroup = parentActivity.findViewById(R.id.radioGroupCanny);
                modeGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    switch (checkedId) {
                        case R.id.cannyContours:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_CANNY_CONT);
                            break;
                        case R.id.cannyBinary:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_CANNY_BIN);
                            break;
                        case R.id.cannyBinaryInv:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_CANNY_BIN_INV);
                            break;
                        case R.id.cannyAdaptive:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE);
                            break;
                        case R.id.cannyAdaptiveInv:
                            frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE_INV);
                            break;
                        default:
                            break;
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_HSV: {
                frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_HSV);
            } break;

            case ImageProcessParam.BG_SUBTRACT_OTSU: {
                frame.setProcMethod(ImageProcessParam.BG_SUBTRACT_OTSU);
            } break;

            default:
                break;
        }
    }

    /**
     *
     * @param position
     */
    private void setupSeekBars(final int position, Frame frame) {

        switch (position) {
            case ImageProcessParam.BG_SUBTRACT_ADAPTIVE: {

                SeekBar maxVal       = parentActivity.findViewById(R.id.seekBarMaxValAdaptive);
                SeekBar blockSize    = parentActivity.findViewById(R.id.seekBarBlockSize);
                SeekBar subtractionC = parentActivity.findViewById(R.id.seekSubtractionConstant);
                SeekBar medianBlur   = parentActivity.findViewById(R.id.seekMedianBlur);
                SeekBar morphKernel  = parentActivity.findViewById(R.id.seekBarMorphKernelAdaptive);

                maxVal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView maxValText = parentActivity.findViewById(R.id.maxVal);
                        maxValText.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                blockSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.bSize);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                subtractionC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.cConstant);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                medianBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.mBlur);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                morphKernel.setVisibility(View.GONE);
                morphKernel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelAdaptiveText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                Switch erode = parentActivity.findViewById(R.id.switch8);
                erode.setChecked(false);
                erode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelAdaptiveText);
                        if (isChecked) {
                            frame.setIgnoreMorphology(false);
                            morphKernel.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                        } else {
                            frame.setIgnoreMorphology(true);
                            morphKernel.setVisibility(View.GONE);
                            text.setVisibility(View.GONE);
                        }
                    }
                });

                Switch invMask = parentActivity.findViewById(R.id.switch9);
                invMask.setChecked(false);
                invMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setInvertMask(true);
                        } else {
                            frame.setInvertMask(false);
                        }
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_BINARY: {

                SeekBar thresholdVal = parentActivity.findViewById(R.id.seekBarThresholdValue);
                SeekBar maxValBinary = parentActivity.findViewById(R.id.seekBarMaxValBinary);
                SeekBar morphKernel  = parentActivity.findViewById(R.id.seekBarMorphKernelBinary);

                thresholdVal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.bThresh);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                maxValBinary.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView maxValText = parentActivity.findViewById(R.id.bMaxThresh);
                        maxValText.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                morphKernel.setVisibility(View.GONE);
                morphKernel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelBinaryText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                Switch erode = parentActivity.findViewById(R.id.switch6);
                erode.setChecked(false);
                erode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelBinaryText);
                        if (isChecked) {
                            frame.setIgnoreMorphology(false);
                            morphKernel.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                        } else {
                            frame.setIgnoreMorphology(true);
                            morphKernel.setVisibility(View.GONE);
                            text.setVisibility(View.GONE);
                        }
                    }
                });

                Switch invMask = parentActivity.findViewById(R.id.switch7);
                invMask.setChecked(false);
                invMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setInvertMask(true);
                        } else {
                            frame.setInvertMask(false);
                        }
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_DIFF: {

                SeekBar learntRate    = parentActivity.findViewById(R.id.seekBarSubtractionLearningRate);
                SeekBar historyFrames = parentActivity.findViewById(R.id.seekBarHistoryFramesNo);
                SeekBar complexity    = parentActivity.findViewById(R.id.seekBarComplexityReduction);
                SeekBar backRatio     = parentActivity.findViewById(R.id.seekBarBackgroundRatio);
                SeekBar morphKernel   = parentActivity.findViewById(R.id.seekBarMorphKernelDiff);

                learntRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float floatVal;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        floatVal = (float)progress / 3000.0f;
                        TextView text = parentActivity.findViewById(R.id.learningRateText);
                        text.setText(Float.toString(floatVal));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                historyFrames.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.historyFramesText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                complexity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float floatVal;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        floatVal = (float)progress / 3000.0f;
                        TextView text = parentActivity.findViewById(R.id.complexityReductionText);
                        text.setText(String.valueOf(floatVal));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                backRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float floatVal;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        floatVal = (float)progress / 20.0f;
                        TextView text = parentActivity.findViewById(R.id.backgroundRatioText);
                        text.setText(String.valueOf(floatVal));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                morphKernel.setVisibility(View.GONE);
                morphKernel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelDiffText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });


                Switch erode = parentActivity.findViewById(R.id.switch10);
                erode.setChecked(false);
                erode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelDiffText);
                        if (isChecked) {
                            frame.setIgnoreMorphology(false);
                            morphKernel.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                        } else {
                            frame.setIgnoreMorphology(true);
                            morphKernel.setVisibility(View.GONE);
                            text.setVisibility(View.GONE);
                        }
                    }
                });

                Switch invMask = parentActivity.findViewById(R.id.switch11);
                invMask.setChecked(false);
                invMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setInvertMask(true);
                        } else {
                            frame.setInvertMask(false);
                        }
                    }
                });

                Switch shadows = parentActivity.findViewById(R.id.switch12);
                shadows.setChecked(false);
                shadows.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setDetectShadows(true);
                        } else {
                            frame.setDetectShadows(false);
                        }
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_HSV: {

                SeekBar lh = parentActivity.findViewById(R.id.seekBarLh);
                SeekBar lv = parentActivity.findViewById(R.id.seekBarLv);
                SeekBar ls = parentActivity.findViewById(R.id.seekBarLs);
                SeekBar uh = parentActivity.findViewById(R.id.seekBarUh);
                SeekBar uv = parentActivity.findViewById(R.id.seekBarUv);
                SeekBar us = parentActivity.findViewById(R.id.seekBarUs);
                SeekBar morphKernel = parentActivity.findViewById(R.id.seekBarMorphKernel);

                lh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.lhText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                lv.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.lvText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                ls.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.lsText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                uh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.uhText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                uv.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView maxValText = parentActivity.findViewById(R.id.uvText);
                        maxValText.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                us.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView maxValText = parentActivity.findViewById(R.id.usText);
                        maxValText.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                morphKernel.setVisibility(View.GONE);
                morphKernel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelText);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                Switch erode = parentActivity.findViewById(R.id.switch4);
                erode.setChecked(false);
                erode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TextView text = parentActivity.findViewById(R.id.morphKernelText);
                        if (isChecked) {
                            frame.setIgnoreMorphology(false);
                            morphKernel.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                        } else {
                            frame.setIgnoreMorphology(true);
                            morphKernel.setVisibility(View.GONE);
                            text.setVisibility(View.GONE);
                        }
                    }
                });

                Switch invMask = parentActivity.findViewById(R.id.switch5);
                invMask.setChecked(false);
                invMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setInvertMask(true);
                        } else {
                            frame.setInvertMask(false);
                        }
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_OTSU: {

                SeekBar thresholdOtsu    = parentActivity.findViewById(R.id.seekBarThresholdValueOtsu);
                SeekBar maxValOtsu       = parentActivity.findViewById(R.id.seekBarMaxValOtsu);
                SeekBar gaussianBlurOtsu = parentActivity.findViewById(R.id.seekGaussianBlurOtsu);

                thresholdOtsu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.thresholdValOtsu);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                maxValOtsu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.maxValOtsu);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                gaussianBlurOtsu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.gaussianBlurOtsu);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            } break;

            case ImageProcessParam.BG_SUBTRACT_CANNY: {

                SeekBar thresholdCanny   = parentActivity.findViewById(R.id.seekBarThresholdValueCanny);
                SeekBar maxValCanny      = parentActivity.findViewById(R.id.seekBarMaxValCanny);

                thresholdCanny.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.bThreshCanny);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                maxValCanny.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView text = parentActivity.findViewById(R.id.bMaxThreshCanny);
                        text.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                Switch hsvOverwrite = parentActivity.findViewById(R.id.switch2);
                hsvOverwrite.setChecked(false);
                hsvOverwrite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setIgnoreMeanHSV(true);
                        } else {
                            frame.setIgnoreMeanHSV(false);
                        }
                    }
                });

                Switch enhance = parentActivity.findViewById(R.id.switch3);
                enhance.setChecked(false);
                enhance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            frame.setIgnoreEnhance(true);
                        } else {
                            frame.setIgnoreEnhance(false);
                        }
                    }
                });
            } break;

            default:
                break;
        }
    }
}