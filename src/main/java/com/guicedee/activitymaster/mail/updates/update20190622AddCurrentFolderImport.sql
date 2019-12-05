With Classifications(id)
         as
         (
             select ClassificationID
             from Classification
             where ClassificationName = 'CurrentFolderImport'
               and EnterpriseID = -9223372036854775807
         ),
     ARR(id)
         as(
         select distinct ar.ArrangementID
         from Arrangement ar
                  inner join ArrangementXArrangementType atx
                             on atx.ArrangementID = ar.ArrangementID
                                 and atx.EffectiveToDate >= getDate()
                  inner join ArrangementType at
                             on atx.ArrangementTypeID = at.ArrangementTypeID
         where at.ArrangementTypeName = 'MailImport'
           and at.EnterpriseID = -9223372036854775807
           and ar.EffectiveToDate >= getDate()
     )
        ,
     Arrangements (id,clid,cllid,systemid,enterpriseid,activeflagid)
         as
         (
             select ar.ArrangementID,max(cl.ClassificationID),max(cl.ClassificationID),
                    max(ar.SystemID),max(ar.EnterpriseID),max(ar.ActiveFlagID)
             from Arrangement ar
                      left join ArrangementXClassification axc
                                on ar.ArrangementID = axc.ArrangementID
                                    and axc.EffectiveToDate >= getDate()
                      left join Classification cl
                                on axc.ClassificationID = cl.ClassificationID
                                    and cl.ClassificationName = 'CurrentFolderImport'
             where ar.ArrangementID in (select id from ARR)
             group by ar.ArrangementID
             --and atx.EffectiveToDate >= getDate()
             --and axc.EffectiveToDate >= getDate()
             --and cl.EffectiveToDate >= getDate()
             --and cl.ClassificationID is null
         )
INSERT INTO ArrangementXClassification ([ArrangementID], [ClassificationID], [Value], [OriginalSourceSystemID], [OriginalSourceSystemUniqueID],[ActiveFlagID], [EnterpriseID], [SystemID])
select
    distinct ar.id,c.id,'INBOX',systemid,'',activeflagid,enterpriseid,systemid
--*
from Arrangements ar
         cross join Classifications c
where cllid is null
