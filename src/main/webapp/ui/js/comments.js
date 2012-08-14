var Comments;

(function($) {
    Comments = {
        disableAddButton : function() {
            $("#add-comment-button").addClass("disabled").attr("disabled",
                    "disabled");
        },
        enableAddButton : function() {
            $("#add-comment-button").removeClass("disabled").removeAttr(
                    "disabled");
        },
        reply : function(commentId) {
            this.setParent(commentId);
            this.placeFormTo("#comment_" + commentId);
        },
        clearTextarea : function() {
            $("#add-comment textarea").val("");
        },
        setParent : function(parentId) {
            $("#add-comment").find("form input[name='parentId']").val(parentId);
        },
        placeFormTo : function(containerId) {
            $("#add-comment").detach().appendTo(containerId);
            $("#add-comment").show();
        },
        foldSubtree: function(commentId) {
            $("#comment_" + commentId + " .replies").slideUp();
        },
        expandSubtree: function(commentId) {
            $("#comment_" + commentId + " .replies").slideDown();
        },
        _mkSlider: function(effect, showOnComplete) {
            return function() {
                var self = $(this);
                var comment = self.closest(".comment");
                var replies = comment.find(".replies");
                replies[effect].call(replies, 'fast', function() {
                    self.hide();
                    comment.find(showOnComplete).first().show();
                });
            };
        }
    };
    $(function() {
        $("#add-comment-button").live("click", function() {
            Comments.clearTextarea();
            $("#add-comment").show();
            Comments.setParent(0);
            Comments.disableAddButton();
            Comments.placeFormTo("#add");
        });
        $("#add-comment .btn-cancel").live("click", function() {
            $("#add-comment").hide();
            $("#preview").remove();
            Comments.enableAddButton();
            Comments.placeFormTo("#add");
        });
        
        $(".foldSubtree").live("click", Comments._mkSlider("slideUp", ".expandSubtree"));
        $(".expandSubtree").live("click", Comments._mkSlider("slideDown", ".foldSubtree"));
        
        $(".reply").live("click", function() {
            Comments.reply($(this).data("comment-id"));
        });
    });
})(jQuery);