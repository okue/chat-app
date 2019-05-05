port module Main exposing (main)

import Bootstrap.Alert as Alert
import Bootstrap.Button as Button
import Bootstrap.CDN as CDN
import Bootstrap.Card as Card
import Bootstrap.Card.Block as Block
import Bootstrap.Grid as Grid
import Bootstrap.Grid.Col as Col
import Bootstrap.Grid.Row as Row
import Bootstrap.ListGroup as Listgroup
import Bootstrap.Modal as Modal
import Bootstrap.Table as Table
import Bootstrap.Text as Text
import Bootstrap.Utilities.Border as Border
import Bootstrap.Utilities.Flex as Flex
import Bootstrap.Utilities.Spacing as Spacing
import Browser exposing (UrlRequest)
import Browser.Dom as Dom
import Browser.Navigation as Navigation
import Dict exposing (Dict)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput)
import Html.Lazy exposing (..)
import Http
import Json.Decode as D
import Json.Encode as E
import Maybe exposing (Maybe, andThen, withDefault)
import Set exposing (Set)
import Task
import Tuple exposing (first, second)



-------------------------------------------------------------------------------
--                               Port
-------------------------------------------------------------------------------


port sendMsg : ( String, String, String ) -> Cmd msg



-------------------------------------------------------------------------------
--                               Types
-------------------------------------------------------------------------------


type alias Model =
    { messagesList : List Message
    , currentTo : User
    , currentContent : String
    }


type Msg
    = ChangedTo User
    | ChangedContent String
    | ClickedPost
    | ClickedDelete Id


type alias Flags =
    {}


type alias Message =
    { from : String
    , to : String
    , content : String
    }


type alias Id =
    Int


type alias User =
    String



-------------------------------------------------------------------------------
--                               Main Functions
-------------------------------------------------------------------------------


main : Program Flags Model Msg
main =
    Browser.document
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


init : Flags -> ( Model, Cmd Msg )
init flags =
    let
        model =
            { messagesList = []
            , currentTo = ""
            , currentContent = ""
            }
    in
    ( model
    , Cmd.none
    )


view : Model -> Browser.Document Msg
view model =
    { title = ""
    , body = mainBody model
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    -- Debug.log ("update is called, " ++ Debug.toString msg) <|
    case msg of
        ChangedContent content ->
            ( { model | currentContent = content }, Cmd.none )

        ChangedTo to ->
            ( { model | currentTo = to }, Cmd.none )

        ClickedPost ->
            let
                newMsg =
                    { from = "todo_change", to = model.currentTo, content = model.currentContent }
            in
            ( { model | currentTo = "", currentContent = "", messagesList = newMsg :: model.messagesList }
            , sendMsg ( "todo_change", model.currentTo, model.currentContent )
            )

        ClickedDelete i ->
            ( model, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-------------------------------------------------------------------------------
--                               Sub Functions
-------------------------------------------------------------------------------


mainBody : Model -> List (Html Msg)
mainBody model =
    let
        toTalkBox message =
            div []
                [ text message.from
                , text " "
                , text message.to
                , text " "
                , text message.content
                ]

        messages =
            List.map toTalkBox model.messagesList
    in
    [ textBox model ] ++ messages


textBox : Model -> Html Msg
textBox model =
    div []
        [ viewInput "text" "to" model.currentTo ChangedTo
        , viewInput "text" "content" model.currentContent ChangedContent
        , button [ onClick ClickedPost ] [ text "post" ]
        ]


viewInput : String -> String -> String -> (String -> msg) -> Html msg
viewInput t p v toMsg =
    input [ type_ t, placeholder p, value v, onInput toMsg ] []
