import { useState, useContext } from 'react'
import { Context } from '../App'

function Option(props) {
  return (
    <>
      <div className="w-full flex gap-[2%] justify-center">
        <div onClick={props.onClick} className="
          h-[5vh] w-[70%] bg-[#2288BB] rounded-[5px] mb-[5vh] px-[2%] [cursor:pointer]
          text-white text-[3vh] grid grid-cols-2 gap-[2%]
        ">
          <div className={props.className1}>{props.text1}</div>
          <div className={props.className2}>{props.text2}</div>
        </div>
      </div>
    </>
  )
}

export default Option;