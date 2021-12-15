package org.opendatakit.fragment;


import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import java.util.Random;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import org.opendatakit.test_utils.TestActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.opendatakit.fragment.AlertNProgessMsgFragmentMger.DialogState;

public class AlertNProgessMsgFragmentMgerTest {

    private static final String APP_NAME = "APP_NAME";

    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";
    private static final String PROGRESS_DIALOG_TAG = "PROGRESS_DIALOG_TAG";

    private static final String ALERT_DIALOG_TITLE = "ALERT_DIALOG_TITLE";
    private static final String ALERT_DIALOG_MESSAGE = "ALERT_DIALOG_MESSAGE";

    private static final String PROGRESS_DIALOG_TITLE = "PROGRESS_DIALOG_TITLE";
    private static final String PROGRESS_DIALOG_MESSAGE = "PROGRESS_DIALOG_TITLE";

    private AlertNProgessMsgFragmentMger systemUnderTest;

    @Rule
    public ActivityScenarioRule<TestActivity> rule = new ActivityScenarioRule<>(TestActivity.class);

    @Before
    public void setUp() {
        systemUnderTest = new AlertNProgessMsgFragmentMger(APP_NAME,
                ALERT_DIALOG_TAG,
                PROGRESS_DIALOG_TAG,
                false,
                false);
    }

    @Test
    public void testNoDialogIsCreated() {
        assertEquals(DialogState.None, systemUnderTest.getDialogState());
    }

    @Test
    public void testAlertDialogIsCreated() {
        ActivityScenario<TestActivity> scenario = rule.getScenario();

        scenario.onActivity(new ActivityScenario.ActivityAction<TestActivity>() {
            @Override
            public void perform(TestActivity activity) {
                int fragmentId = new Random().nextInt();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                systemUnderTest.createAlertDialog(ALERT_DIALOG_TITLE, ALERT_DIALOG_MESSAGE, fragmentManager, fragmentId);
                fragmentManager.executePendingTransactions();

                assertThat(fragmentManager.findFragmentByTag(ALERT_DIALOG_TAG).isAdded(), is(true));
                assertEquals(DialogState.Alert, systemUnderTest.getDialogState());
                Assert.assertThat(systemUnderTest.hasDialogBeenCreated(), is(true));
            }
        });
    }

    @Test
    public void testProgressDialogIsCreated() {
        ActivityScenario<TestActivity> scenario = rule.getScenario();

        scenario.onActivity(new ActivityScenario.ActivityAction<TestActivity>() {
            @Override
            public void perform(TestActivity activity) {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                systemUnderTest.createProgressDialog(PROGRESS_DIALOG_TITLE, PROGRESS_DIALOG_MESSAGE, fragmentManager);
                fragmentManager.executePendingTransactions();

                assertThat(fragmentManager.findFragmentByTag(PROGRESS_DIALOG_TAG).isAdded(), is(true));
                assertEquals(DialogState.Progress, systemUnderTest.getDialogState());
                Assert.assertThat(systemUnderTest.hasDialogBeenCreated(), is(true));
            }
        });
    }

    @Test
    public void testProgressDialogIsDismissed() {
        ActivityScenario<TestActivity> scenario = rule.getScenario();

        scenario.onActivity(new ActivityScenario.ActivityAction<TestActivity>() {
            @Override
            public void perform(TestActivity activity) {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                systemUnderTest.createProgressDialog(PROGRESS_DIALOG_TITLE, PROGRESS_DIALOG_MESSAGE, fragmentManager);
                fragmentManager.executePendingTransactions();
                systemUnderTest.dismissProgressDialog(fragmentManager);

                assertEquals(DialogState.None, systemUnderTest.getDialogState());
            }
        });
    }

    @Test
    public void testAlertDialogIsDismissed() {
        ActivityScenario<TestActivity> scenario = rule.getScenario();

        scenario.onActivity(new ActivityScenario.ActivityAction<TestActivity>() {
            @Override
            public void perform(TestActivity activity) {
                int fragmentId = new Random().nextInt();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                systemUnderTest.createAlertDialog(ALERT_DIALOG_TITLE, ALERT_DIALOG_MESSAGE, fragmentManager, fragmentId);
                fragmentManager.executePendingTransactions();
                systemUnderTest.dismissAlertDialog(fragmentManager);

                assertEquals(DialogState.None, systemUnderTest.getDialogState());
            }
        });
    }
}