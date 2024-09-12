//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.commands;

import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.parsing.ParserException;

// ParserException doesn't seem to be specific to parsing...? And it's convenient with its whole caption system.
public class CommandError extends ParserException {

  public static final Caption JAIL_FAILED_PLAYER_NEVER_JOINED = Caption.of("jailFailedPlayerNeverJoined");
  public static final Caption JAIL_FAILED_PLAYER_EXEMPT = Caption.of("jailFailedPlayerExempt");
  public static final Caption INFO_FAILED_PLAYER_NOT_JAILED = Caption.of("infoFailedPlayerNotJailed");
  public static final Caption UNJAIL_FAILED_PLAYER_NOT_JAILED = Caption.of("unjailFailedPlayerNotJailed");

  public static final Caption RESOLVE_JAIL_FAILED = Caption.of("non-existent-jail");
  public static final Caption RESOLVE_PRISONER_FAILED = Caption.of("player-not-imprisoned");
  public static final Caption RELOAD_FAILED = Caption.of("reload-failed");
  public static final Caption SAVE_ALL_FAILED = Caption.of("save-all-failed");
  public static final Caption SAVE_JAIL_FAILED = Caption.of("save-jail-failed");
  public static final Caption DELETE_JAIL_FAILED = Caption.of("delete-jail-failed");

  public static CaptionVariable prisonerVariable(final String name) {
    return CaptionVariable.of("prisoner", name);
  }

  public static CaptionVariable executorVariable(final String name) {
    return CaptionVariable.of("player", name);
  }

  public static CaptionVariable jailVariable(final String name) {
    return CaptionVariable.of("jail", name);
  }

  public static CaptionVariable timeVariable(final String name) {
    return CaptionVariable.of("player", name);
  }

  public CommandError(final CommandContext<?> ctx, final Caption caption, final CaptionVariable... variables) {
    super(CommandHandler.class, ctx, caption, variables);
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }
}
