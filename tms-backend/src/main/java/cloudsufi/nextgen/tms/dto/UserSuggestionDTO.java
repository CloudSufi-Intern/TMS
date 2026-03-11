package cloudsufi.nextgen.tms.dto;

/**
 * Projection used for returning lightweight user suggestions
 * in search/autocomplete operations.
 * @author vishwasvaidya
 */

public interface UserSuggestionDTO {
     Long getId();
     String getUsername();
}
