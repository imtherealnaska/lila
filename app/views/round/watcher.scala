package views.html
package round

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._
import lila.common.HTTPRequest
import lila.common.String.html.safeJsonValue
import lila.game.Pov

import controllers.routes

object watcher {

  def apply(
    pov: Pov,
    data: play.api.libs.json.JsObject,
    tour: Option[lila.tournament.TourMiniView],
    simul: Option[lila.simul.Simul],
    cross: Option[lila.game.Crosstable.WithMatchup],
    userTv: Option[lila.user.User] = None,
    chatOption: Option[lila.chat.UserChat.Mine],
    bookmarked: Boolean
  )(implicit ctx: Context) = {

    val chatJson = chatOption map { c =>
      chat.json(c.chat, name = trans.spectatorRoom.txt(), timeout = c.timeout, withNote = ctx.isAuth, public = true)
    }

    bits.layout(
      variant = pov.game.variant,
      title = gameVsText(pov.game, withRatings = true),
      moreJs = frag(
        roundNvuiTag,
        roundTag,
        embedJs(s"""window.lichess=window.lichess||{};customWS=true;onload=function(){
LichessRound.boot({data:${safeJsonValue(data)},i18n:${jsI18n(pov.game)},chat:${jsOrNull(chatJson)}})}""")
      ),
      openGraph = povOpenGraph(pov).some,
      chessground = false
    )(
        main(cls := "round")(
          st.aside(cls := "round__side")(bits.side(pov, data, tour, simul, userTv, bookmarked)),
          chatOption.map(_ => chat.frag),
          bits.roundAppPreload(pov, false),
          div(cls := "round__underboard")(bits.crosstable(cross, pov.game)),
          div(cls := "round__underchat")(bits underchat pov.game)
        )
      )
  }

  def crawler(pov: Pov, initialFen: Option[chess.format.FEN], pgn: chess.format.pgn.Pgn)(implicit ctx: Context) =
    bits.layout(
      variant = pov.game.variant,
      title = gameVsText(pov.game, withRatings = true),
      openGraph = povOpenGraph(pov).some,
      chessground = false
    )(frag(
        main(cls := "round")(
          st.aside(cls := "round__side")(
            game.side(pov, initialFen, none, simul = none, userTv = none, bookmarked = false),
            div(cls := "for-crawler")(
              h1(titleGame(pov.game)),
              p(describePov(pov)),
              div(cls := "pgn")(pgn.render)
            )
          ),
          div(cls := "round__board main-board")(board.bits.domPreload(pov.some))
        )
      ))
}
