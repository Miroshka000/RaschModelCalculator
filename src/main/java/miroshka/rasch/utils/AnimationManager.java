package miroshka.rasch.utils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class AnimationManager {
    
    private static final Duration DEFAULT_DURATION = Duration.millis(300);
    private static final Duration FAST_DURATION = Duration.millis(150);
    private static final Duration SLOW_DURATION = Duration.millis(500);
    
    private AnimationManager() {
    }
    
    public static CompletableFuture<Void> fadeIn(Node node) {
        return fadeIn(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> fadeIn(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(Interpolator.EASE_OUT);
            return fade;
        });
    }
    
    public static CompletableFuture<Void> fadeOut(Node node) {
        return fadeOut(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> fadeOut(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setInterpolator(Interpolator.EASE_IN);
            return fade;
        });
    }
    
    public static CompletableFuture<Void> scaleIn(Node node) {
        return scaleIn(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> scaleIn(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            ScaleTransition scale = new ScaleTransition(duration, node);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(Interpolator.EASE_OUT);
            return scale;
        });
    }
    
    public static CompletableFuture<Void> scaleOut(Node node) {
        return scaleOut(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> scaleOut(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            ScaleTransition scale = new ScaleTransition(duration, node);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.8);
            scale.setToY(0.8);
            scale.setInterpolator(Interpolator.EASE_IN);
            return scale;
        });
    }
    
    public static CompletableFuture<Void> slideInFromLeft(Node node) {
        return slideInFromLeft(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> slideInFromLeft(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromX(-50);
            translate.setToX(0);
            translate.setInterpolator(Interpolator.EASE_OUT);
            return translate;
        });
    }
    
    public static CompletableFuture<Void> slideInFromRight(Node node) {
        return slideInFromRight(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> slideInFromRight(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromX(50);
            translate.setToX(0);
            translate.setInterpolator(Interpolator.EASE_OUT);
            return translate;
        });
    }
    
    public static CompletableFuture<Void> slideInFromTop(Node node) {
        return slideInFromTop(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> slideInFromTop(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromY(-30);
            translate.setToY(0);
            translate.setInterpolator(Interpolator.EASE_OUT);
            return translate;
        });
    }
    
    public static CompletableFuture<Void> slideInFromBottom(Node node) {
        return slideInFromBottom(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> slideInFromBottom(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromY(30);
            translate.setToY(0);
            translate.setInterpolator(Interpolator.EASE_OUT);
            return translate;
        });
    }
    
    public static CompletableFuture<Void> rotate360(Node node) {
        return rotate360(node, SLOW_DURATION);
    }
    
    public static CompletableFuture<Void> rotate360(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            RotateTransition rotate = new RotateTransition(duration, node);
            rotate.setFromAngle(0);
            rotate.setToAngle(360);
            rotate.setInterpolator(Interpolator.EASE_BOTH);
            return rotate;
        });
    }
    
    public static CompletableFuture<Void> pulse(Node node) {
        return pulse(node, FAST_DURATION);
    }
    
    public static CompletableFuture<Void> pulse(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            ScaleTransition scaleUp = new ScaleTransition(duration.divide(2), node);
            scaleUp.setFromX(1.0);
            scaleUp.setFromY(1.0);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);
            
            ScaleTransition scaleDown = new ScaleTransition(duration.divide(2), node);
            scaleDown.setFromX(1.1);
            scaleDown.setFromY(1.1);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_IN);
            
            ParallelTransition parallel = new ParallelTransition();
            scaleUp.setOnFinished(e -> scaleDown.play());
            parallel.getChildren().add(scaleUp);
            
            return parallel;
        });
    }
    
    public static CompletableFuture<Void> fadeInAndSlideUp(Node node) {
        return fadeInAndSlideUp(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> fadeInAndSlideUp(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(Interpolator.EASE_OUT);
            
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromY(20);
            translate.setToY(0);
            translate.setInterpolator(Interpolator.EASE_OUT);
            
            ScaleTransition scale = new ScaleTransition(duration, node);
            scale.setFromX(0.95);
            scale.setFromY(0.95);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(Interpolator.EASE_OUT);
            
            ParallelTransition parallel = new ParallelTransition();
            parallel.getChildren().addAll(fade, translate, scale);
            
            return parallel;
        });
    }
    
    public static CompletableFuture<Void> fadeOutAndSlideDown(Node node) {
        return fadeOutAndSlideDown(node, DEFAULT_DURATION);
    }
    
    public static CompletableFuture<Void> fadeOutAndSlideDown(Node node, Duration duration) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");
        
        return createAnimation(() -> {
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setInterpolator(Interpolator.EASE_IN);
            
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromY(0);
            translate.setToY(20);
            translate.setInterpolator(Interpolator.EASE_IN);
            
            ScaleTransition scale = new ScaleTransition(duration, node);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.95);
            scale.setToY(0.95);
            scale.setInterpolator(Interpolator.EASE_IN);
            
            ParallelTransition parallel = new ParallelTransition();
            parallel.getChildren().addAll(fade, translate, scale);
            
            return parallel;
        });
    }
    
    public static void resetTransforms(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        
        node.setOpacity(1.0);
        node.setScaleX(1.0);
        node.setScaleY(1.0);
        node.setTranslateX(0.0);
        node.setTranslateY(0.0);
        node.setRotate(0.0);
    }
    
    private static CompletableFuture<Void> createAnimation(AnimationSupplier supplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            javafx.animation.Animation animation = supplier.get();
            animation.setOnFinished(e -> future.complete(null));
            animation.play();
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    @FunctionalInterface
    private interface AnimationSupplier {
        javafx.animation.Animation get() throws Exception;
    }
    
    public static final class Builder {
        private Node node;
        private Duration duration = DEFAULT_DURATION;
        
        private Builder() {
        }
        
        public static Builder forNode(Node node) {
            Builder builder = new Builder();
            builder.node = Objects.requireNonNull(node, "Node cannot be null");
            return builder;
        }
        
        public Builder withDuration(Duration duration) {
            this.duration = Objects.requireNonNull(duration, "Duration cannot be null");
            return this;
        }
        
        public Builder withFastDuration() {
            this.duration = FAST_DURATION;
            return this;
        }
        
        public Builder withSlowDuration() {
            this.duration = SLOW_DURATION;
            return this;
        }
        
        public CompletableFuture<Void> fadeIn() {
            return AnimationManager.fadeIn(node, duration);
        }
        
        public CompletableFuture<Void> fadeOut() {
            return AnimationManager.fadeOut(node, duration);
        }
        
        public CompletableFuture<Void> scaleIn() {
            return AnimationManager.scaleIn(node, duration);
        }
        
        public CompletableFuture<Void> scaleOut() {
            return AnimationManager.scaleOut(node, duration);
        }
        
        public CompletableFuture<Void> slideInFromLeft() {
            return AnimationManager.slideInFromLeft(node, duration);
        }
        
        public CompletableFuture<Void> slideInFromRight() {
            return AnimationManager.slideInFromRight(node, duration);
        }
        
        public CompletableFuture<Void> slideInFromTop() {
            return AnimationManager.slideInFromTop(node, duration);
        }
        
        public CompletableFuture<Void> slideInFromBottom() {
            return AnimationManager.slideInFromBottom(node, duration);
        }
        
        public CompletableFuture<Void> pulse() {
            return AnimationManager.pulse(node, duration);
        }
        
        public CompletableFuture<Void> rotate360() {
            return AnimationManager.rotate360(node, duration);
        }
        
        public CompletableFuture<Void> fadeInAndSlideUp() {
            return AnimationManager.fadeInAndSlideUp(node, duration);
        }
        
        public CompletableFuture<Void> fadeOutAndSlideDown() {
            return AnimationManager.fadeOutAndSlideDown(node, duration);
        }
        
        public void reset() {
            AnimationManager.resetTransforms(node);
        }
    }
}
