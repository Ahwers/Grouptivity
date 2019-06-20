package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ahwers.grouptivity.Models.ExtraAttribute;
import com.ahwers.grouptivity.R;


public class AttributeFragment extends DialogFragment {

    private static final String TAG = "AttributeFragment";

    private static final String ARG_TITLE = "attribute_title";
    private static final String ARG_VALUE = "attribute_value";
    private static final String ARG_IS_NEW = "new_attribute";

    private static final String EXTRA_ATTRIBUTE_TITLE = "com.book.grouptivity.attribute_title";
    private static final String EXTRA_ATTRIBUTE_VALUE = "com.book.grouptivity.attribute_value";
    private static final String EXTRA_DELETE_ATTRIBUTE = "com.book.grouptivity.attribute_deleted";

    private EditText mAttributeTitle;
    private EditText mAttributeValue;

    public static AttributeFragment newInstance(ExtraAttribute attribute, boolean isNew) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, attribute.getTitle());
        args.putString(ARG_VALUE, attribute.getValue());
        args.putBoolean(ARG_IS_NEW, isNew);

        AttributeFragment fragment = new AttributeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String getAttributeTitle(Intent data) {
        return data.getStringExtra(EXTRA_ATTRIBUTE_TITLE);
    }

    public static String getExtraAttributeValue(Intent data) {
        return data.getStringExtra(EXTRA_ATTRIBUTE_VALUE);
    }

    public static boolean getDeleteAttribute(Intent data) {
        return data.getBooleanExtra(EXTRA_DELETE_ATTRIBUTE, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String attributeTitle = getArguments().getString(ARG_TITLE);
        String attributeValue = getArguments().getString(ARG_VALUE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_attribute, null);

        mAttributeTitle = (EditText) v.findViewById(R.id.attribute_title);
        mAttributeTitle.setText(attributeTitle);

        mAttributeValue = (EditText) v.findViewById(R.id.attribute_value);
        mAttributeValue.setText(attributeValue);

        AlertDialog dialog;

        if (getArguments().getBoolean(ARG_IS_NEW)) {
            dialog = new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle(attributeTitle == null && attributeValue == null ? R.string.create_attribute_title : R.string.update_attribute_title)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ExtraAttribute attribute = new ExtraAttribute(mAttributeTitle.getText().toString(), mAttributeValue.getText().toString());
                                    sendResult(Activity.RESULT_OK, attribute);
                                }
                            })
                    .create();
        } else {
            dialog = new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle(attributeTitle == null && attributeValue == null ? R.string.create_attribute_title : R.string.update_attribute_title)
                    .setNegativeButton(R.string.delete,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendResult(Activity.RESULT_OK, null);
                                }
                            })
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ExtraAttribute attribute = new ExtraAttribute(mAttributeTitle.getText().toString(), mAttributeValue.getText().toString());
                                    sendResult(Activity.RESULT_OK, attribute);
                                }
                            })
                    .create();
        }

        return dialog;
    }

    private void sendResult(int resultCode, ExtraAttribute attribute) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        if (attribute == null) {
            intent.putExtra(EXTRA_DELETE_ATTRIBUTE, true);
        } else {
            intent.putExtra(EXTRA_ATTRIBUTE_TITLE, attribute.getTitle());
            intent.putExtra(EXTRA_ATTRIBUTE_VALUE, attribute.getValue());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
