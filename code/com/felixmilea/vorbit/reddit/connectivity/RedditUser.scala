package com.felixmilea.vorbit.reddit.connectivity

case class RedditUser(val credential: Credential, var session: Session = null, val id: Int = -1) {
  def hasValidSession(): Boolean = session != null && session.isValid
}