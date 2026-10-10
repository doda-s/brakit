package com.brakit.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Used to create a member and, as a full replacement, to update one: an omitted role clears it
public record MemberRequest(
	@NotBlank @Size(max = 255) String name,
	// Optional free-text description; @Pattern accepts null but rejects blank text, reusing @NotBlank's localized message
	@Pattern(regexp = NOT_BLANK, message = "{jakarta.validation.constraints.NotBlank.message}") @Size(max = 255) String role
) {
	private static final String NOT_BLANK = "(?s).*\\S.*";
}
