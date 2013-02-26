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
        scrollTo: function(target) {
            return $.scrollTo(target, 500, {"offset": -200});
        },
        placeFormTo : function(containerId) {
            var addCommentDiv = $("#add-comment");
            addCommentDiv.detach().appendTo(containerId);
            addCommentDiv.show();
            this.scrollTo(addCommentDiv);
            addCommentDiv.find("textarea").focus();
        },
        _mkSlider: function(effect, showOnComplete) {
            return function() {
                var self = $(this);
                var comment = self.closest(".comment");
                var replies = comment.children(".replies");
                replies[effect].call(replies, 'fast', function() {
                    self.hide();
                    comment.find(showOnComplete).first().show();
                });
            };
        }
    };
    $(function() {
        $("#add-comment-button").on("click", function() {
            Comments.clearTextarea();
            $("#add-comment").show();
            Comments.setParent(0);
            Comments.disableAddButton();
            Comments.placeFormTo("#add");
        });
        $("#add-comment .btn-cancel").on("click", function() {
            $("#preview").remove();
            Comments.enableAddButton();
            Comments.placeFormTo("#add");
            $("#add-comment").hide();
        });
        
        $(".foldSubtree").on("click", Comments._mkSlider("slideUp", ".expandSubtree"));
        $(".expandSubtree").on("click", Comments._mkSlider("slideDown", ".foldSubtree"));
        
        $(".reply").on("click", function() {
            Comments.reply($(this).data("comment-id"));
        });
        
        $(".comment-link").on("click", function() {
           Comments.scrollTo(this); 
        });
        
        if (document.location.hash) {
            var document_hash = document.location.hash;
            var reg = /^\#comment_(\d+)$/;
            var matches = document_hash.match(reg);
            var comment;
            if (matches) {
                comment = $(document_hash);
                comment.addClass('highlight');
                Comments.scrollTo(comment);
            }
        }

    });
})(jQuery);
