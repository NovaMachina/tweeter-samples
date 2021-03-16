package edu.byu.cs.tweeter.presenter;

import android.util.Log;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.service.FollowingService;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;

/**
 * The presenter for the "following" functionality of the application.
 */
public class FollowingPresenter implements FollowingService.Observer {

    private static final String LOG_TAG = "FollowingPresenter";
    private static final int PAGE_SIZE = 10;

    private final View view;
    private final User user;
    private final AuthToken authToken;

    private User lastFollowee;
    private boolean hasMorePages = true;
    private boolean isLoading = false;

    /**
     * The interface by which this presenter communicates with it's view.
     */
    public interface View {
        // Specify methods here that will be called on the view in response to model updates

        /**
         * Called to notify the view when data loading starts and ends.
         *
         * @param value true if we are loading, false otherwise.
         */
        void setLoading(boolean value);

        /**
         * Called to pass "following" users to the view when they are loaded.
         *
         * @param newUsers list of new "following" users.
         */
        void addItems(List<User> newUsers);

        /**
         * Directs the view to display the specified error message to the user.
         *
         * @param message error message to be displayed.
         */
        void displayErrorMessage(String message);
    }

    /**
     * Creates an instance.
     *
     * @param view the view for which this class is the presenter.
     * @param user the user that is currently logged in.
     * @param authToken the auth token for the current session.
     */
     public FollowingPresenter(View view, User user, AuthToken authToken) {
        this.view = view;
        this.user = user;
        this.authToken = authToken;
    }

    User getLastFollowee() {
        return lastFollowee;
    }

    boolean isHasMorePages() {
        return hasMorePages;
    }

    boolean isLoading() {
        return isLoading;
    }

    /**
     * Called by the view to request that another page of "following" users be loaded.
     */
    public void loadMoreItems() {
        if (!isLoading && hasMorePages) {
            isLoading = true;
            view.setLoading(true);

            FollowingRequest request = new FollowingRequest(user.getAlias(), PAGE_SIZE, (lastFollowee == null ? null : lastFollowee.getAlias()));
            getFollowing(request);
        }
    }

    /**
     * Requests the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned for a previous request. This is an asynchronous
     * operation.
     *
     * @param request contains the data required to fulfill the request.
     */
    public void getFollowing(FollowingRequest request) {
        getFollowingService(this).getFollowees(request);
    }

    /**
     * Returns an instance of {@link FollowingService}. Allows mocking of the FollowingService class
     * for testing purposes. All usages of FollowingService should get their FollowingService
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    public FollowingService getFollowingService(FollowingService.Observer observer) {
        return new FollowingService(observer);
    }

    /**
     * Notifies the view when the users requested by the call to
     * {@link #getFollowing(FollowingRequest)} have been retrieved.
     *
     * @param followingResponse the response.
     */
    @Override
    public void followeesRetrieved(FollowingResponse followingResponse) {
        List<User> followees = followingResponse.getFollowees();

        lastFollowee = (followees.size() > 0) ? followees.get(followees.size() -1) : null;
        hasMorePages = followingResponse.getHasMorePages();

        isLoading = false;
        view.setLoading(false);
        view.addItems(followees);
    }

    /**
     * A callback indicating that an exception occurred in an asynchronous method this class is
     * observing. Notifies the view of the exception.
     *
     * @param exception the exception.
     */
    @Override
    public void handleException(Exception exception) {
        Log.e(LOG_TAG, exception.getMessage(), exception);
        isLoading = false;
        view.setLoading(false);
        view.displayErrorMessage(exception.getMessage());
    }
}
