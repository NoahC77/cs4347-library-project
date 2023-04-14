function Field(props) {

  return (
    <>
      <div className="w-full flex gap-[2%] justify-center mb-[5vh]">
        <div className="h-[5vh] w-[70%] flex">
          <div className="
            h-[5vh] w-[40%] text-[3vh] text-black
          ">
            {props.text1}
          </div>
          {props.editable ?
            <input placeholder={props.text2} className="
              h-[5vh] w-[60%] bg-[#EAEAEA] rounded-[5px] px-[2%]
              placeholder:text-[#999999] text-[3vh]
            "/>
          :
            <div className="
              h-[5vh] w-[60%] bg-[#EAEAEA] rounded-[5px] px-[2%]
              text-black text-[3vh] leading-[5vh]
            ">
              {props.text2}
            </div>
          }
        </div>
      </div>
    </>
  )
}

export default Field;