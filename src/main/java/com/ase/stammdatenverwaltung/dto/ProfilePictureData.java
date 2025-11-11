package com.ase.stammdatenverwaltung.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data transfer object for profile picture content and metadata.
 *
 * <p>Carries profile picture bytes along with content-type information to enable proper HTTP
 * response handling (Content-Type header).
 */
@Getter
@AllArgsConstructor
public class ProfilePictureData {
  private final byte[] picture;
  private final String contentType;
}
