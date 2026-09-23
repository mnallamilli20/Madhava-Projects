package tables.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tables.course.Course;
import tables.course.CourseRepository;
import tables.user.User;
import tables.user.UserRepository;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Review saveReviewAndUpdateCourse(Review review) {

        User managedUser = userRepository.findById(review.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + review.getUser().getUserId()));
        Course managedCourse = courseRepository.findById(review.getCourse().getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + review.getCourse().getCourseId()));

        review.setUser(managedUser);
        review.setCourse(managedCourse);

        Review savedReview = reviewRepository.save(review);

        Long courseId = savedReview.getCourse().getCourseId();
        List<Review> allReviews = reviewRepository.findByCourse_CourseId(courseId);

        double totalOverall = 0;
        double totalDifficulty = 0;
        double totalWorkload = 0;

        for (Review r : allReviews) {
            totalOverall += r.getOverallRating();
            totalDifficulty += r.getDifficultyRating();
            totalWorkload += r.getWorkloadRating();
        }

        int count = allReviews.size();

        Course course = savedReview.getCourse();
        course.setReviewCount(count);
        course.setAvgOverall(totalOverall / count);
        course.setAvgDifficulty(totalDifficulty / count);
        course.setAvgWorkload(totalWorkload / count);

        courseRepository.save(course);

        return savedReview;
    }

    @Transactional
    public void deleteReviewAndUpdateCourse(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Course course = review.getCourse();
        reviewRepository.delete(review);

        List<Review> remainingReviews = reviewRepository.findByCourse_CourseId(course.getCourseId());

        if (remainingReviews.isEmpty()) {
            course.setReviewCount(0);
            course.setAvgOverall(0.0);
            course.setAvgDifficulty(0.0);
            course.setAvgWorkload(0.0);
        } else {
            double totalOverall = 0;
            double totalDifficulty = 0;
            double totalWorkload = 0;

            for (Review r : remainingReviews) {
                totalOverall += r.getOverallRating();
                totalDifficulty += r.getDifficultyRating();
                totalWorkload += r.getWorkloadRating();
            }

            int count = remainingReviews.size();
            course.setReviewCount(count);
            course.setAvgOverall(totalOverall / count);
            course.setAvgDifficulty(totalDifficulty / count);
            course.setAvgWorkload(totalWorkload / count);
        }

        courseRepository.save(course);
    }
}