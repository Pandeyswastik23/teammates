package teammates.ui.webapi.action;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.ui.webapi.output.FeedbackSessionData;

/**
 * Unpublish a feedback session.
 */
public class UnpublishFeedbackSessionAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        String feedbackSessionName = getNonNullRequestParamValue(Const.ParamsNames.FEEDBACK_SESSION_NAME);

        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, userInfo.getId());
        FeedbackSessionAttributes session = logic.getFeedbackSession(feedbackSessionName, courseId);

        gateKeeper.verifyAccessible(instructor, session,
                Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION);
    }

    @Override
    public ActionResult execute() {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        String feedbackSessionName = getNonNullRequestParamValue(Const.ParamsNames.FEEDBACK_SESSION_NAME);

        FeedbackSessionAttributes session = logic.getFeedbackSession(feedbackSessionName, courseId);

        try {
            logic.unpublishFeedbackSession(session);
            if (session.isPublishedEmailEnabled()) {
                taskQueuer.scheduleFeedbackSessionUnpublishedEmail(session.getCourseId(), session.getFeedbackSessionName());
            }
        } catch (EntityDoesNotExistException e) {
            return new JsonResult(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (InvalidParametersException e) {
            return new JsonResult(e.getMessage(), HttpStatus.SC_BAD_REQUEST);
        }

        session = logic.getFeedbackSession(feedbackSessionName, courseId);
        return new JsonResult(new FeedbackSessionData(session));
    }

}
