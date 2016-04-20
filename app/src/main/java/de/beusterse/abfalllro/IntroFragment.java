package de.beusterse.abfalllro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Selects the matching intro page layout file
 *
 * Created by Felix Beuster
 */
public class IntroFragment extends Fragment {

    private static String PAGE = "page";

    private int page;

    public static IntroFragment newInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE, position);

        IntroFragment introFragment = new IntroFragment();
        introFragment.setArguments(bundle);

        return introFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(PAGE)) {
            throw new RuntimeException("missing " + PAGE + " argument");
        }
        page = getArguments().getInt(PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        int layout;
        switch (page) {
            case 0:
                layout = R.layout.intro_page_setup_location;
                break;
            case 1:
                layout = R.layout.intro_page_setup_schedule;
                break;
            case 2:
                layout = R.layout.intro_page_guide_main;
                break;
            case 3:
                layout = R.layout.intro_page_guide_preview;
                break;
            default:
                layout = R.layout.intro_page_guide_settings;
        }

        View view = getActivity().getLayoutInflater().inflate(layout, container, false);
        view.setTag(page);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
